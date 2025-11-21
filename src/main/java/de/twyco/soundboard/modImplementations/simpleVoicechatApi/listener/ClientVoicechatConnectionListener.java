package de.twyco.soundboard.modImplementations.simpleVoicechatApi.listener;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.twyco.soundboard.Soundboard;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.SimpleVoicechatApi;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.SimpleVoicechatService;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.interfaces.SimpleVoicechatClientEventListener;

import java.util.UUID;

public class ClientVoicechatConnectionListener implements SimpleVoicechatClientEventListener<ClientVoicechatConnectionEvent> {

    @Override
    public void onEvent(ClientVoicechatConnectionEvent e) {
        VoicechatClientApi clientApi = e.getVoicechat();
        if (e.isConnected()) {
            SimpleVoicechatService.setClientApi(clientApi);
            VolumeCategory volumeCategory = clientApi
                    .volumeCategoryBuilder()
                    .setId(Soundboard.MOD_ID + "sounds")
                    .setName("Soundboard")
                    .build();
            clientApi.registerClientVolumeCategory(volumeCategory);

            ClientStaticAudioChannel audioChannel = clientApi.createStaticAudioChannel(UUID.randomUUID());
            audioChannel.setCategory(volumeCategory.getId());

            SimpleVoicechatApi.setSoundboardVolumeCategory(volumeCategory);
            SimpleVoicechatApi.setAudioChannel(audioChannel);
        } else {
            clientApi.unregisterClientVolumeCategory(SimpleVoicechatApi.getSoundboardVolumeCategory());
            SimpleVoicechatService.clearClientApi();
        }
        SimpleVoicechatService.stopAllSounds();
    }
}
