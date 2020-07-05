package ru.abramov.filemanager.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class NickNameSender {
    private static final String SERVER = "NickNameSender: ";

    public static void send(String nickname, Channel channel) {
        ByteBuf buf = null;
//        отправка сигнального байта
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 22);
        channel.writeAndFlush(buf);
        System.out.println(SERVER + "отправка сигнального байта" + buf);
//       длинна никнейма
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(nickname.length());
        channel.writeAndFlush(buf);
//        никнейм
        byte[] loginBytes = nickname.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(loginBytes.length);
        buf.writeBytes(loginBytes);
        channel.writeAndFlush(buf);
    }
}