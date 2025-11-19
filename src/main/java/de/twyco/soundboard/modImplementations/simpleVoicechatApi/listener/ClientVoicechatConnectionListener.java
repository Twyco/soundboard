package de.twyco.soundboard.modImplementations.simpleVoicechatApi.listener;

import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.SimpleVoicechatService;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.interfaces.SimpleVoicechatClientEventListener;

public class ClientVoicechatConnectionListener implements SimpleVoicechatClientEventListener<ClientVoicechatConnectionEvent> {

    @Override
    public void onEvent(ClientVoicechatConnectionEvent e) {
        if(e.isConnected()) {
            SimpleVoicechatService.setClientApi(e.getVoicechat());
        } else {
            SimpleVoicechatService.clearClientApi();
        }
        SimpleVoicechatService.stopAllSounds();
    }
}
