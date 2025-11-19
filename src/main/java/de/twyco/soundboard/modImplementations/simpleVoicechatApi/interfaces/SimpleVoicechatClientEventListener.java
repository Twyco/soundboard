package de.twyco.soundboard.modImplementations.simpleVoicechatApi.interfaces;

import de.maxhenkel.voicechat.api.events.ClientEvent;
import de.twyco.soundboard.interfaces.VoicechatListener;

public interface SimpleVoicechatClientEventListener<T extends ClientEvent> extends VoicechatListener<T> {
    void onEvent(T e);
}
