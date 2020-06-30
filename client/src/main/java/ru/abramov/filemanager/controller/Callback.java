package ru.abramov.filemanager.controller;

import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

public interface Callback {
    void callback(Object... args);
}
