package ru.abramov.filemanager.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ByteProtocolHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        WAIT, NAME_LENGTH, NAME, FILE_LENGTH, FILE, LOGIN_LENGTH, LOGIN, PASSWORD_LENGTH, PASSWORD
    }

    public enum Act {
        GET_FILE((byte) 32), AUTH((byte) 22);
        private byte act;

        Act(byte act) {
            this.act = act;
        }

        public byte getActByte() {
            return act;
        }
    }

    private String stigma = "server_";
    private State currentState = State.WAIT;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private Path clientPath;

    private String nettyServerPath = NettyServer.getServerPath().normalize().toAbsolutePath().toString();

    private String clientName;
    private String nickname;
    private static String login;
    private static final List<Channel> channels = new ArrayList<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connect :" + ctx);
        channels.add(ctx.channel()); // добавили слиента в лист
        clientName = "DefaultClient";
        clientPath = Paths.get(nettyServerPath + "\\" + clientName);
        if (!Files.exists(clientPath)) {
            Files.createDirectory(clientPath);
        }
        NettyServer.setServerPath(clientPath);
        System.out.println("SERVER: " + "Подключился новый клиент " + clientName);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент " + clientName + "вышел из сети." + "\nНа сервере " + channels.size() + " клиентов.");
        channels.remove(ctx.channel());
        System.out.println("SERVER: " + "Клиент " + clientName + "вышел из сети" + "На сервере " + channels.size() + " клиентов.");
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
//            Сигнальный байт
            if (currentState == State.WAIT) {
                byte readed = buf.readByte();
                if (readed == Act.GET_FILE.getActByte()) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("Сигнальный байт =" + readed + " = копирование файла");
                } else if (readed == Act.AUTH.getActByte()) {
                    currentState = State.LOGIN_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("Сигнальный байт = " + readed + " = авторизация");
                } else {
                    System.out.println("Invalid first byte - " + readed);
                }
            }
//              Получаем длинну имени файла
            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("Получаем длинну имени файла");
                    nextLength = buf.readInt();
                    System.out.println(nextLength);
                    currentState = State.NAME;
                }
            }
//              Получаем имя файла и открываем поток прописывая новое имя файла
            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    System.out.println("Получаем имя файла и открываем поток прописывая новое имя файла - " + new String(fileName, "UTF-8"));
                    out = new BufferedOutputStream(new FileOutputStream(clientPath + "\\" + stigma + new String(fileName)));
                    currentState = State.FILE_LENGTH;
                }
            }
//              Получаем длинну файла
            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("Получаем длинну файла - " + fileLength);
                    currentState = State.FILE;
                }
            }
//              записываем файл
            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    System.out.println(receivedFileLength);
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.WAIT;
                        System.out.println("Файл получен!");
                        out.close();
                        break;
                    }
                }
            }
            /**
             * Авторизация
             */
//            Получаем длинну логина
            if (currentState == State.LOGIN_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("Получаем длинну Login");
                    nextLength = buf.readInt();
                    System.out.println("Длинна логина = " + nextLength);
                    currentState = State.LOGIN;
                }
            }
//            Получаем логин
            if (currentState == State.LOGIN) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] clientLoginBuf = new byte[nextLength];
                    buf.readBytes(clientLoginBuf);
                    login = new String(clientLoginBuf, "UTF-8");
                    System.out.println("Получаем login =" + login);
                    currentState = State.PASSWORD_LENGTH;
                }
            }

//            Получаем длинну пароля
            if (currentState == State.PASSWORD_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    nextLength = buf.readInt();
                    System.out.println("Длинна пароля = " + nextLength);
                    currentState = State.PASSWORD;
                    System.out.println(currentState);
                }
            }
//            Получаем пароль
            if (currentState == State.PASSWORD) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] clientPasswordBuf = new byte[nextLength];
                    buf.readBytes(clientPasswordBuf);
                    String password = new String(clientPasswordBuf, "UTF-8");
                    System.out.println("Получаем пароль = " + password);
                    nickname = SqlClient.getNickname(login, password);
                    if (nickname != null) {
                        System.out.println("nickname= " + nickname);
                        NickNameSender.send(nickname, ctx.channel());
                        clientPath = Paths.get(nettyServerPath + "\\" + nickname);
                        if (!Files.exists(clientPath)) {
                            Files.createDirectory(clientPath);
                        }
                    } else {
                        ctx.channel().close();
                        System.out.println("ctx.channel().isActive()" + ctx.channel().isActive());
                    }
                    currentState = State.WAIT;
                }
            }

        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
