package ru.abramov.filemanager.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class StringSender {
    private static final String LOGER = "StringSender: ";

    public static void send(String str, Channel channel, SignalByte signalByte) {
        ByteBuf buf = null;
//        отправка сигнального байта
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(signalByte.getActByte());
        System.out.println(LOGER + " отправка сигнального байта" + signalByte);
        channel.writeAndFlush(buf);
//       длинна строки
        byte[] stringBytes = str.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(stringBytes.length);
        channel.writeAndFlush(buf);
//        строка
        buf = ByteBufAllocator.DEFAULT.directBuffer(stringBytes.length);
        buf.writeBytes(stringBytes);
        channel.writeAndFlush(buf);
        System.out.println(LOGER + " отправка завершена");
    }
}