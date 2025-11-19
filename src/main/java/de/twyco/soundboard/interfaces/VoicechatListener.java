package de.twyco.soundboard.interfaces;

import java.util.EventListener;

public interface VoicechatListener<T> extends EventListener {
    void onEvent(T e);
}
