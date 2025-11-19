package de.twyco.soundboard.modImplementations.simpleVoicechatApi.listener;

import de.maxhenkel.voicechat.api.events.MergeClientSoundEvent;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.SimpleVoicechatService;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.interfaces.SimpleVoicechatClientEventListener;

public class MergeClientSoundListener implements SimpleVoicechatClientEventListener<MergeClientSoundEvent> {

    @Override
    public void onEvent(MergeClientSoundEvent e) {
        SimpleVoicechatService.mixInto(e);
    }
}