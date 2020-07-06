package ru.abramov.filemanager.netty.server;

public enum SignalByte {
    GET_FILE((byte) 32), AUTH((byte) 22), SET_LIST_FILE((byte)11);
    private byte act;

    SignalByte(byte act) {
        this.act = act;
    }

    public byte getActByte() {
        return act;
    }
}