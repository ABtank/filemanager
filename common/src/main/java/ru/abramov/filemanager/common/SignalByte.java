package ru.abramov.filemanager.common;

public enum SignalByte {
    GET_FILE((byte) 32),
    AUTH((byte) 22), CHANGE_NICKNAME((byte) 23),
    SET_LIST_FILE((byte)11), REQUEST_FILE((byte)33),REQUEST_DELETE_FILE((byte)34),
    UPDATE_LIST_SERVER((byte)321), CLEAR_LIST_SERVER((byte) 666);
    private byte act;

    SignalByte(byte act) {
        this.act = act;
    }

    public byte getActByte() {
        return act;
    }
}