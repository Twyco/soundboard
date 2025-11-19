package de.twyco.soundboard.modImplementations.simpleVoicechatApi;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MergeClientSoundEvent;
import de.twyco.soundboard.Soundboard;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.listener.ClientVoicechatConnectionListener;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.listener.MergeClientSoundListener;

public class SimpleVoicechatApi implements VoicechatPlugin {

    private static VoicechatApi voicechatApi = null;

    @Override
    public String getPluginId() {
        return Soundboard.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi voicechatApi) {
        SimpleVoicechatApi.voicechatApi = voicechatApi;
        Soundboard.LOGGER.info("[SimpleVoicechatApi/initialize] VoicechatApi initialized");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(
                ClientVoicechatConnectionEvent.class,
                new ClientVoicechatConnectionListener()::onEvent,
                0
        );
        registration.registerEvent(
                MergeClientSoundEvent.class,
                new MergeClientSoundListener()::onEvent,
                100
        );
    }

    public static VoicechatApi getVoicechatApi() {
        return voicechatApi;
    }

    public static boolean isAvailable() {
        return voicechatApi != null;
    }
}
