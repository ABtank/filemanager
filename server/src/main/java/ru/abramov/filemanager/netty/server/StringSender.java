package ru.abramov.filemanager.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class StringSender {
    private static final String LOGER = "StringSender: ";

    public static void send(String nickname, Channel channel, SignalByte signalByte) {
        ByteBuf buf = null;
//        отправка сигнального байта
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(signalByte.getActByte());
        System.out.println(LOGER + " отправка сигнального байта" + signalByte);
        channel.writeAndFlush(buf);
//       длинна строки
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(nickname.length());
        channel.writeAndFlush(buf);
//        строка
        byte[] loginBytes = nickname.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(loginBytes.length);
        buf.writeBytes(loginBytes);
        channel.writeAndFlush(buf);
        System.out.println(LOGER + " отправка завершена");
    }
}