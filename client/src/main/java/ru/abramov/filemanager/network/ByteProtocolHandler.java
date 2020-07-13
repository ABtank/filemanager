package ru.abramov.filemanager.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.abramov.filemanager.controller.Controller;

import java.io.BufferedOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;

public class ByteProtocolHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        WAIT, NAME_LENGTH, NAME, FILE_LENGTH, FILE, NICKNAME_LENGTH, NICKNAME,FILE_LIST_LENGTH,FILELIST
    }

    public enum SignalByte {
        GET_FILE((byte) 32), AUTH((byte) 22), SET_LIST_FILE((byte) 11);
        private byte act;

        SignalByte(byte act) {
            this.act = act;
        }

        public byte getActByte() {
            return act;
        }
    }

    private static final String CLIENT = "CLIENT ByteProtocolHandler: ";
    private State currentState = State.WAIT;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private Path clientPath;


    private String nickname;

    public String getNickname() {
        return nickname;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(CLIENT + "Client connect :" + ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
//            Сигнальный байт
            if (currentState == State.WAIT) {
                byte signalByte = buf.readByte();
                if (signalByte == SignalByte.GET_FILE.getActByte()) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println(CLIENT + "Сигнальный байт =" + signalByte + " = копирование файла");
                } else if (signalByte == SignalByte.AUTH.getActByte()) {
                    currentState = State.NICKNAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println(CLIENT + "Сигнальный байт = " + signalByte + " = авторизация");
                } else if (signalByte == SignalByte.SET_LIST_FILE.getActByte()) {
                    currentState = State.FILE_LIST_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println(CLIENT + "Сигнальный байт = " + signalByte + " = получение списка файлов");
                } else {
                    System.out.println(CLIENT + "Invalid first byte - " + signalByte);
                }
            }

//            Получаем длинну nickname
            getLengthNickname(buf);
//            Получаем nickname
            getNickname(buf);

//            Получаем длинну слроки
            getLengthFileList(buf);
//            Получаем строку
            getFileList(buf);
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void getLengthNickname(ByteBuf buf) {
        if (currentState == State.NICKNAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                System.out.println(CLIENT + "Получаем длинну nickname");
                nextLength = buf.readInt();
                System.out.println(CLIENT + "Длинна nickname = " + nextLength);
                currentState = State.NICKNAME;
            }
        }
    }

    private void getNickname(ByteBuf buf) throws UnsupportedEncodingException {
        if (currentState == State.NICKNAME) {
            if (buf.readableBytes() >= nextLength) {
                byte[] clientLoginBuf = new byte[nextLength];
                buf.readBytes(clientLoginBuf);
                nickname = new String(clientLoginBuf, "UTF-8");
                System.out.println(CLIENT + "Получаем nickname =" + nickname);
                Controller.setNickname(nickname);
                currentState = State.WAIT;
            }
        }
    }

    private void getLengthFileList(ByteBuf buf) {
        if (currentState == State.FILE_LIST_LENGTH) {
            if (buf.readableBytes() >= 4) {
                System.out.println(CLIENT + "Получаем длинну nickname");
                nextLength = buf.readInt();
                System.out.println(CLIENT + "Длинна nickname = " + nextLength);
                currentState = State.FILELIST;
            }
        }
    }

    private void getFileList(ByteBuf buf) throws UnsupportedEncodingException {
        if (currentState == State.FILELIST) {
            if (buf.readableBytes() >= nextLength) {
                byte[] clientLoginBuf = new byte[nextLength];
                buf.readBytes(clientLoginBuf);
                nickname = new String(clientLoginBuf, "UTF-8");
                System.out.println(CLIENT + "Получаем nickname =" + nickname);
                currentState = State.WAIT;
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
