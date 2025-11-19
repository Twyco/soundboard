package de.twyco.soundboard.modImplementations.simpleVoicechatApi;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.MergeClientSoundEvent;
import de.maxhenkel.voicechat.api.mp3.Mp3Decoder;
import de.twyco.soundboard.Soundboard;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.util.PlayingSound;
import de.twyco.soundboard.util.client.SoundboardRuntimeState;
import de.twyco.soundboard.util.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class SimpleVoicechatService {

    private static final Logger LOG = Soundboard.LOGGER;
    private static VoicechatClientApi clientApi = null;
    private static final List<PlayingSound> activeSounds = new ArrayList<>();


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

        synchronized (activeSounds) {
            for (PlayingSound ps : activeSounds) {
                if(ps.soundId.equals(sound.getId())) {
                    if(ps.loop) {
                        activeSounds.remove(ps);
                        return;
                    }
                    ps.index = 0;
                    LOG.info("[SimpleVoicechatService/playSound] Restart playing sound '{}' (active={})",
                            sound.getName(), activeSounds.size());
                    return;
                }
            }
            activeSounds.add(new PlayingSound(sound, samples));
            LOG.info("[SimpleVoicechatService/playSound] Queued sound '{}' (active={})",
                    sound.getName(), activeSounds.size());
        }
    }

    public static boolean isAvailable() {
        return clientApi != null;
    }

    public static void setClientApi(VoicechatClientApi api) {
        clientApi = api;
        LOG.info("[SimpleVoicechatService/setClientApi] ClientApi set: {}", api.getClass().getName());
    }

    public static void clearClientApi() {
        clientApi = null;
        stopAllSounds();
        LOG.info("[SimpleVoicechatService/clearClientApi] ClientApi cleared");
    }

    //--------------------------------------

    public static void mixInto(MergeClientSoundEvent event) {
        synchronized (activeSounds) {
            if (activeSounds.isEmpty()) {
                return;
            }
        }

        int chunkSize = 960; // 20ms @ 48kHz, mono TODO CHECK
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

        if(SoundboardRuntimeState.isPlayWhileMuted() || !clientApi.isMuted()) {
            event.mergeAudio(mixed);
        }
    }

    private static short[] decodeSoundToPcm(@NotNull Sound sound, VoicechatClientApi api) {
        Path path = sound.getPath();
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);

        try (InputStream in = Files.newInputStream(path)) {
            if (fileName.endsWith(".mp3")) {
                Mp3Decoder decoder = api.createMp3Decoder(in);
                if (decoder == null) {
                    LOG.warn("[SimpleVoicechatService/decodeSoundToPcm] Mp3Decoder is null (not supported?)");
                    return null;
                }

                short[] stereoOrMono = decoder.decode();
                return stereoToMono(stereoOrMono);
            } else {
                LOG.warn("[SimpleVoicechatService/decodeSoundToPcm] Unsupported format for now: {}", fileName);
                return null;
            }
        } catch (IOException e) {
            LOG.error("[SimpleVoicechatService/decodeSoundToPcm] Failed to read sound file '{}': {}",
                    path, e.getMessage());
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

    public static List<PlayingSound> getCurrentlyPlayingSounds() {
        synchronized (activeSounds) {
            return activeSounds;
        }
    }
}
