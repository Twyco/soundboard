package de.twyco.soundboard.modImplementations.simpleVoicechatApi;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.MergeClientSoundEvent;
import de.maxhenkel.voicechat.api.mp3.Mp3Decoder;
import de.twyco.soundboard.Soundboard;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.util.PlayingSound;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.config.SoundboardConfigData;
import de.twyco.soundboard.util.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sound.sampled.AudioFormat;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class SimpleVoicechatService {

    private static final Logger LOG = Soundboard.LOGGER;
    private static VoicechatClientApi clientApi = null;
    private static final List<PlayingSound> activeSounds = new ArrayList<>();

    private static final int TARGET_SAMPLE_RATE = 48_000;

    private SimpleVoicechatService() {
    }

    public static void stopAllSounds() {
        synchronized (activeSounds) {
            activeSounds.clear();
        }
    }

    public static void playSound(@NotNull Sound sound) {
        if (!isAvailable()) {
            LOG.error("[SimpleVoicechatService/playSound] is not available");
            return;
        }

        short[] samples = decodeSoundToPcm(sound, clientApi);
        if(samples == null || samples.length == 0) {
            LOG.error("[SimpleVoicechatService/playSound] No PCM data for '{}'", sound.getName());
            return;
        }
        double seconds = (double) samples.length / TARGET_SAMPLE_RATE;
        LOG.debug("Decoded samples: {}, duration ~{}s", samples.length, seconds);

        synchronized (activeSounds) {
            for (PlayingSound ps : activeSounds) {
                if(ps.soundId.equals(sound.getId())) {
                    activeSounds.remove(ps);
                    return;
                }
            }
            activeSounds.add(new PlayingSound(
                    sound,
                    samples,
                    SoundboardConfig.get().globalState.getSoundAmplifier()
            ));
            LOG.debug("[SimpleVoicechatService/playSound] Queued sound '{}' (active={})",
                    sound.getName(), activeSounds.size());
        }
    }

    public static boolean isAvailable() {
        return clientApi != null;
    }

    public static void setClientApi(VoicechatClientApi api) {
        clientApi = api;
        LOG.debug("[SimpleVoicechatService/setClientApi] ClientApi set: {}", api.getClass().getName());
    }

    public static void clearClientApi() {
        clientApi = null;
        stopAllSounds();
        LOG.debug("[SimpleVoicechatService/clearClientApi] ClientApi cleared");
    }

    public static List<PlayingSound> getCurrentlyPlayingSounds() {
        synchronized (activeSounds) {
            return activeSounds;
        }
    }

    public static Set<String> getCurrentlyPlayingSoundIds() {
        synchronized (activeSounds) {
            Set<String> soundIds = new HashSet<>();
            for (PlayingSound sound : activeSounds) {
                soundIds.add(sound.soundId);
            }
            return soundIds;
        }
    }

    //-------------------- helper --------------------

    public static void mixInto(MergeClientSoundEvent event) {
        SoundboardConfigData config = SoundboardConfig.get();
        synchronized (activeSounds) {
            if (activeSounds.isEmpty()) {
                return;
            }
        }

        int chunkSize = 960;
        short[] mixed = new short[chunkSize];
        boolean any = false;

        synchronized (activeSounds) {
            Iterator<PlayingSound> it = activeSounds.iterator();
            while (it.hasNext()) {
                PlayingSound ps = it.next();

                if (ps.isFinished()) {
                    it.remove();
                    continue;
                }

                if (ps.samples.length == 0) {
                    continue;
                }

                any = true;

                int remaining = ps.samples.length - ps.index;
                if (remaining <= 0 && ps.loop) {
                    ps.index = 0;
                    remaining = ps.samples.length;
                }

                int copy = Math.min(chunkSize, remaining);

                for (int i = 0; i < copy; i++) {
                    int sampleIndex = ps.index + i;

                    if (sampleIndex >= ps.samples.length) {
                        if (ps.loop) {
                            sampleIndex %= ps.samples.length;
                        } else {
                            break;
                        }
                    }


                    int s = (int) (ps.samples[sampleIndex] * ps.gain);

                    int v = mixed[i] + s;
                    if (v > Short.MAX_VALUE) {
                        v = Short.MAX_VALUE;
                    } else if (v < Short.MIN_VALUE) {
                        v = Short.MIN_VALUE;
                    }
                    mixed[i] = (short) v;
                }

                ps.index += copy;
            }
        }

        if (!any) {
            return;
        }

        if(config.globalState.playWhileMuted || !clientApi.isMuted()) {
            event.mergeAudio(mixed);
            SimpleVoicechatApi.getAudioChannel().play(mixed);
        }
    }

    private static short[] decodeSoundToPcm(@NotNull Sound sound, VoicechatClientApi api) {
        Path path = sound.getPath();
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);

        try (InputStream in = Files.newInputStream(path)) {
            if (!fileName.endsWith(".mp3")) {
                LOG.warn("[SimpleVoicechatService/decodeSoundToPcm] Unsupported format for now: {}", fileName);
                return null;
            }

            Mp3Decoder decoder = api.createMp3Decoder(in);
            if (decoder == null) {
                LOG.warn("[SimpleVoicechatService/decodeSoundToPcm] Mp3Decoder is null (not supported?)");
                return null;
            }

            short[] raw = decoder.decode();
            AudioFormat format = decoder.getAudioFormat();

            float srcRate = format.getSampleRate();
            int channels = format.getChannels();

            short[] mono = raw;
            if(channels == 2) {
                mono = stereoToMono(raw);
            } else if(channels != 1) {
                LOG.warn("[SimpleVoicechatService/decodeSoundToPcm] Unsupported channel count: {}", channels);
                return  null;
            }

            short[] pcm48k = mono;
            if (Math.round(srcRate) != TARGET_SAMPLE_RATE) {
                pcm48k = resampleLinear(mono, srcRate, TARGET_SAMPLE_RATE);
            }

            LOG.debug("Decoded '{}' {}Hz/{}ch -> {}Hz/mono, samples={}",
                    sound.getName(), (int) srcRate, channels, TARGET_SAMPLE_RATE, pcm48k.length);

            return pcm48k;
        } catch (Exception e) {
            LOG.error("[SimpleVoicechatService/decodeSoundToPcm] Failed to decode sound file '{}': {}",
                    path, e.getMessage());
        }

        return null;
    }

    private static short[] stereoToMono(short[] stereo) {
        if (stereo == null) {
            return null;
        }

        if (stereo.length % 2 != 0) {
            return stereo;
        }

        int monoLength = stereo.length / 2;
        short[] mono = new short[monoLength];

        for (int i = 0, j = 0; i < monoLength; i++, j += 2) {
            int left = stereo[j];
            int right = stereo[j + 1];
            mono[i] = (short) ((left + right) / 2);
        }

        return mono;
    }

    private static short[] resampleLinear(short[] in, float srcRate, int dstRate) {
        if (in == null || in.length == 0) return in;
        if (Math.round(srcRate) == dstRate) return in;

        double ratio = dstRate / (double) srcRate;
        int outLen = (int) Math.round(in.length * ratio);
        if (outLen <= 0) return new short[0];

        short[] out = new short[outLen];
        double step = srcRate / (double) dstRate;

        for (int i = 0; i < outLen; i++) {
            double srcPos = i * step;
            int i0 = (int) Math.floor(srcPos);
            int i1 = Math.min(i0 + 1, in.length - 1);
            double frac = srcPos - i0;

            double v = (1.0 - frac) * in[i0] + frac * in[i1];
            int iv = (int) Math.round(v);
            if (iv > Short.MAX_VALUE) iv = Short.MAX_VALUE;
            if (iv < Short.MIN_VALUE) iv = Short.MIN_VALUE;
            out[i] = (short) iv;
        }
        return out;
    }
}
