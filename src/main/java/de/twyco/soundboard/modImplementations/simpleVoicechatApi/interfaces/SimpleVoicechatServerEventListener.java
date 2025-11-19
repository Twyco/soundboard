package de.twyco.soundboard.modImplementations.simpleVoicechatApi.interfaces;

import de.maxhenkel.voicechat.api.events.ServerEvent;
import de.twyco.soundboard.interfaces.VoicechatListener;

public interface SimpleVoicechatServerEventListener<T extends ServerEvent> extends VoicechatListener<T> {
    void onEvent(T e);
}
