package ru.abramov.filemanager.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class StringSender {

    public static void sendAuth(String login, String password, Channel channel) {
//        отправка сигнального байта
        sendSignalByte(channel ,SignalByte.AUTH);
//        логина
        sendString(login, channel);
//        пароля
        sendString(password,channel);
    }

    public static void sendFileList(String list, Channel channel) {
//        отправка сигнального байта
        sendSignalByte(channel ,SignalByte.SET_LIST_FILE);
//        ллист
        sendString(list, channel);
    }

    public static void sendSignalByte(Channel channel, SignalByte signalByte) {
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(signalByte.getActByte());
        channel.writeAndFlush(buf);
        System.out.println("отправка сигнального байта" + buf);
    }

    public static void sendString(String str, Channel channel) {
        ByteBuf buf= null;
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(strBytes.length);
        channel.writeAndFlush(buf);
//        строка
        buf = ByteBufAllocator.DEFAULT.directBuffer(strBytes.length);
        buf.writeBytes(strBytes);
        channel.writeAndFlush(buf);
    }
}
