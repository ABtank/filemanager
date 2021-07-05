package ru.abramov.filemanager.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

public class MainHandler extends SimpleChannelInboundHandler<String> {
    private static final List<Channel> channels = new ArrayList<>();
    private static int newClientIndex = 1;
    private String clientName;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connect :" + ctx);
        channels.add(ctx.channel()); // добавили слиента в лист
        clientName = "Client№" + newClientIndex;
        newClientIndex++;
        broadcastMessage("SERVER ","Подключился новый клиент "+clientName);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент "+ clientName + "вышел из сети." +"\nНа сервере "+channels.size()+" клиентов.");
        channels.remove(ctx.channel());
        broadcastMessage("SERVER", "Клиент "+ clientName + "вышел из сети" +"На сервере "+channels.size()+" клиентов.");
        ctx.close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        System.out.println("get message" + s);
        if (s.startsWith("/")) {
            if (s.startsWith("/copyFile ")) {

            }
            if (s.startsWith("/moveFile ")) {

            }
            if (s.startsWith("/deleteFile ")) {

            }
            if (s.startsWith("/changeName ")) {
                String newClientName = s.split("\\s", 2)[1];  //бьем строку на 2 части и присваиваем вторую часть
                broadcastMessage("SERVER", "Клиент "+ clientName+"сменил ник на "+newClientName);
                clientName = newClientName;
            }
            return;
        }
        broadcastMessage(clientName, s);
    }

    public void broadcastMessage(String clientName, String msg) {
        String out = String.format("[%s]: %s\n", clientName, msg);
        for (Channel c : channels
        ) {
            c.writeAndFlush(out);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String s = "Клиент "+ clientName + "вышел из сети не по своей воле." +"\nНа сервере "+channels.size()+" клиентов.";
        System.out.println(s);
        channels.remove(ctx.channel());
        broadcastMessage("SERVER", s);
        cause.printStackTrace();
        ctx.close();
    }
}
