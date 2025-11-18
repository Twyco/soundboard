package de.twyco.soundboard.modImplementations.simpleVoicechatApi;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.twyco.soundboard.Soundboard;
import de.twyco.soundboard.util.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;


public class SimpleVoicechatService {

    private static final Logger LOG = Soundboard.LOGGER;

    private SimpleVoicechatService() {
    }

    public static void stopAllSounds() {
        //TODO
    }

    public static void playSound(@NotNull Sound sound) {
        VoicechatApi api = SimpleVoicechatApi.getVoicechatApi();
        if (api == null) {
            LOG.warn("[SimpleVoicechatService/playSound] VoicechatApi is not available");
            return;
        }
        //TODO
    }

    public static void onClientVoicechatConnection(ClientVoicechatConnectionEvent event) {
        LOG.info("[] VoiceClient is connected:{}", event.isConnected());
        stopAllSounds();
    }
}
