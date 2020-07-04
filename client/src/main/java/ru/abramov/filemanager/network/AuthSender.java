package ru.abramov.filemanager.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class AuthSender {

    public static void sendAuth(String login, String password, Channel channel) {
//        отправка сигнального байта
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 22);
        channel.writeAndFlush(buf);
        System.out.println("отправка сигнального байта" + buf);
//        длинна логина
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(login.length());
        channel.writeAndFlush(buf);
//        логин
        byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(loginBytes.length);
        buf.writeBytes(loginBytes);
        channel.writeAndFlush(buf);
//        длинна пароля
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(password.length());
        channel.writeAndFlush(buf);
//        password
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        System.out.println(password);
        buf = ByteBufAllocator.DEFAULT.directBuffer(passwordBytes.length);
        buf.writeBytes(passwordBytes);
        channel.writeAndFlush(buf);
    }
}
