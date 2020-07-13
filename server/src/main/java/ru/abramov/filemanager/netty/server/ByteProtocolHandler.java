package ru.abramov.filemanager.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ru.abramov.filemanager.common.FileSender;
import ru.abramov.filemanager.common.SignalByte;
import ru.abramov.filemanager.common.StringSender;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ByteProtocolHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        WAIT, NAME_FILE_LENGTH, NAME, FILE_LENGTH, FILE,
        LOGIN_LENGTH, LOGIN, PASSWORD_LENGTH, PASSWORD
    }

    public ByteProtocolHandler(Controller controller) {
        this.controller = controller;
    }

    private String stigma = "server_";
    private State currentState = State.WAIT;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private Path clientPath;
    private Controller controller;

    private String nettyServerPath = NettyServer.getServerPath().normalize().toAbsolutePath().toString();

    private String nickname;
    private static String login;
    private static final List<Channel> channels = new ArrayList<>();
    private static final String LOGER = "Server ByteProtocolHandler: ";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        controller.setTfLogServer(LOGER + "Client connect :" + ctx);
        channels.add(ctx.channel()); // добавили слиента в лист
        controller.setTfLogServer(LOGER + "Подключился новый клиент " + nickname);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        controller.setTfLogServer(LOGER + "Клиент " + nickname + "вышел из сети." + "\nНа сервере " + channels.size() + " клиентов.");
        channels.remove(ctx.channel());
        controller.setTfLogServer(LOGER + "Клиент " + nickname + "вышел из сети" + "На сервере " + channels.size() + " клиентов.");
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
//            Сигнальный байт
            if (currentState == State.WAIT) {
                byte signalByte = buf.readByte();
                if (signalByte == SignalByte.GET_FILE.getActByte()) {
                    currentState = State.NAME_FILE_LENGTH;
                    receivedFileLength = 0L;
                    controller.setTfLogServer(LOGER + "Сигнальный байт =" + signalByte + " = копирование файла");
                } else if (signalByte == SignalByte.AUTH.getActByte()) {
                    currentState = State.LOGIN_LENGTH;
                    receivedFileLength = 0L;
                    controller.setTfLogServer(LOGER + "Сигнальный байт = " + signalByte + " = авторизация");
                } else {
                    controller.setTfLogServer(LOGER + "Invalid first byte - " + signalByte);
                }
            }
//              Получаем длинну имени файла
            if (currentState == State.NAME_FILE_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    controller.setTfLogServer(LOGER + "Получаем длинну имени файла");
                    nextLength = buf.readInt();
                    controller.setTfLogServer("" + nextLength);
                    currentState = State.NAME;
                }
            }
//              Получаем имя файла и открываем поток прописывая новое имя файла
            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileNameByte = new byte[nextLength];
                    buf.readBytes(fileNameByte);
                    String fileName = new String(fileNameByte, "UTF-8");
                    controller.setTfLogServer(LOGER + "Получаем имя файла и открываем поток прописывая новое имя файла - " + fileName);
                    out = new BufferedOutputStream(new FileOutputStream(clientPath + "\\" + stigma + fileName));
                    currentState = State.FILE_LENGTH;
                }
            }
//              Получаем длинну файла
            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    controller.setTfLogServer(LOGER + "Получаем длинну файла - " + fileLength);
                    currentState = State.FILE;
                }
            }
//              записываем файл
            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                   // if (receivedFileLength % 100 == 0) controller.setTfLogServer("" + receivedFileLength * 0.01);
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.WAIT;
                        controller.setTfLogServer(LOGER + "Файл получен!");
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
                    controller.setTfLogServer(LOGER + "Получаем длинну Login");
                    nextLength = buf.readInt();
                    controller.setTfLogServer(LOGER + "Длинна логина = " + nextLength);
                    currentState = State.LOGIN;
                }
            }
//            Получаем логин
            if (currentState == State.LOGIN) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] clientLoginBuf = new byte[nextLength];
                    buf.readBytes(clientLoginBuf);
                    login = new String(clientLoginBuf, "UTF-8");
                    controller.setTfLogServer(LOGER + "Получаем login =" + login);
                    currentState = State.PASSWORD_LENGTH;
                }
            }

//            Получаем длинну пароля
            if (currentState == State.PASSWORD_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    nextLength = buf.readInt();
                    controller.setTfLogServer(LOGER + "Длинна пароля = " + nextLength);
                    currentState = State.PASSWORD;
                }
            }
//            Получаем пароль
            if (currentState == State.PASSWORD) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] clientPasswordBuf = new byte[nextLength];
                    buf.readBytes(clientPasswordBuf);
                    String password = new String(clientPasswordBuf, "UTF-8");
                    controller.setTfLogServer(LOGER + "Получаем пароль = " + password);
                    nickname = SqlClient.getNickname(login, password);
                    if (nickname != null) {
                        controller.setTfLogServer(LOGER + "nickname= " + nickname);
                        StringSender.sendSignalByte(ctx.channel(), SignalByte.AUTH);
                        StringSender.sendString(nickname, ctx.channel());
                        clientPath = Paths.get(nettyServerPath + "\\" + nickname);
                        if (!Files.exists(clientPath)) {
                            controller.setTfLogServer(LOGER + "создаем путь для нового пользователя\n" + clientPath);
                            Files.createDirectory(clientPath);
                        }
                        NettyServer.setServerPath(clientPath);
                        getFilesList();
                        StringSender.sendFileList(clientPath.toString(), ctx.channel());
                    } else {
                        ctx.channel().close();
                        controller.setTfLogServer(LOGER + "ctx.channel().isActive()" + ctx.channel().isActive());
                    }
                    controller.setTfLogServer(LOGER + " " + State.WAIT);
                    currentState = State.WAIT;
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    public void getFilesList() {

    }

    private void sendFile(ChannelHandlerContext ctx,String fileName) {
        try {
            // обработка завершения передачи файла через листнер future
            FileSender.sendFile(Paths.get(clientPath.toString(),fileName), ctx.channel(), future -> {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
                if (future.isSuccess()) {
                    System.out.println("Файл " + fileName + " успешно передан");
                    //serverPC.updateList(Paths.get(serverPC.getCurrentPath()));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось отправить файл" + fileName, ButtonType.OK);
            alert.showAndWait();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
