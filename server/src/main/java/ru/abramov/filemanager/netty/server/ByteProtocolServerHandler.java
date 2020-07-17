package ru.abramov.filemanager.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.abramov.filemanager.common.FileInfo;
import ru.abramov.filemanager.common.FileSender;
import ru.abramov.filemanager.common.SignalByte;
import ru.abramov.filemanager.common.StringSender;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ByteProtocolServerHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        WAIT,
        NAME_FILE_LENGTH_FO_GET, NAME, FILE_LENGTH, FILE,
        NAME_FILE_LENGTH_FO_SEND, SEND_FILE,
        NAME_FILE_LENGTH_FO_DELETE, DELETE_FILE,
        LOGIN_LENGTH, LOGIN, PASSWORD_LENGTH, PASSWORD,
        UPDATE_LIST
    }

    public ByteProtocolServerHandler(Controller controller) {
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
    private static final String LOGER = "Server ByteProtocolServerHandler: ";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        controller.setTfLogServer(LOGER + "Client connect : " + ctx);
        channels.add(ctx.channel()); // добавили слиента в лист
        controller.setTfLogServer(LOGER + "Подключился новый клиент " + nickname);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
        controller.setTfLogServer(LOGER + "Клиент " + nickname + " вышел из сети.\n" + "На сервере " + channels.size() + " клиентов.");
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
                    currentState = State.NAME_FILE_LENGTH_FO_GET;
                    receivedFileLength = 0L;
                    controller.setTfLogServer(LOGER + "Сигнальный байт =" + signalByte + " = копирование файла");
                } else if (signalByte == SignalByte.AUTH.getActByte()) {
                    currentState = State.LOGIN_LENGTH;
                    receivedFileLength = 0L;
                    controller.setTfLogServer(LOGER + "Сигнальный байт = " + signalByte + " = авторизация");
                } else if (signalByte == SignalByte.REQUEST_FILE.getActByte()) {
                    currentState = State.NAME_FILE_LENGTH_FO_SEND;
                    receivedFileLength = 0L;
                    controller.setTfLogServer(LOGER + "Сигнальный байт = " + signalByte + " = запрос на отправку файла");
                } else if (signalByte == SignalByte.REQUEST_DELETE_FILE.getActByte()) {
                    currentState = State.NAME_FILE_LENGTH_FO_DELETE;
                    receivedFileLength = 0L;
                    controller.setTfLogServer(LOGER + "Сигнальный байт = " + signalByte + " = запрос удаления файла");
                } else {
                    controller.setTfLogServer(LOGER + "Invalid first byte - " + signalByte);
                }
            }
//              Получаем длинну имени файла
            if (currentState == State.NAME_FILE_LENGTH_FO_GET
                    || currentState == State.NAME_FILE_LENGTH_FO_SEND
                    || currentState == State.NAME_FILE_LENGTH_FO_DELETE) {
                if (buf.readableBytes() >= 4) {
                    controller.setTfLogServer(LOGER + "Получаем длинну имени файла");
                    nextLength = buf.readInt();
                    controller.setTfLogServer("" + nextLength);
                    if (currentState == State.NAME_FILE_LENGTH_FO_GET) {
                        currentState = State.NAME;
                    } else if (currentState == State.NAME_FILE_LENGTH_FO_DELETE) {
                        currentState = State.DELETE_FILE;
                    } else currentState = State.SEND_FILE;
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

            if (currentState == State.DELETE_FILE) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileNameByte = new byte[nextLength];
                    buf.readBytes(fileNameByte);
                    String fileName = new String(fileNameByte, "UTF-8");
                    controller.setTfLogServer(LOGER + "запрашивается файл - " + fileName);
                    Path path = Paths.get(clientPath.toString(), fileName);
                    controller.setTfLogServer(path.toString());
                    if (Files.exists(path)) {
                        if (!Files.isDirectory(path)) {
                            Files.delete(path);
                            controller.setTfLogServer(fileName + " удален");
                            updateFilesList(ctx, clientPath);
                        }
                    } else {
                        controller.setTfLogServer("Файл на удаление не найден");
                    }
                    currentState = State.WAIT;
                }
            }

            if (currentState == State.SEND_FILE) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileNameByte = new byte[nextLength];
                    buf.readBytes(fileNameByte);
                    String fileName = new String(fileNameByte, "UTF-8");
                    controller.setTfLogServer(LOGER + "запрашивается файл - " + fileName);
                    if (Files.exists(Paths.get(clientPath.toString(), fileName))) {
                        try {
                            FileSender.sendFile(Paths.get(clientPath.toString(), fileName), ctx.channel(), future -> {
                                if (!future.isSuccess()) {
                                    future.cause().printStackTrace();
                                }
                                if (future.isSuccess()) {
                                    System.out.println("Файл " + fileName + " успешно передан");
                                    updateFilesList(ctx, clientPath);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                            controller.setTfLogServer("Не удалось отправить файл " + fileName);
                        }
                    } else {
                        controller.setTfLogServer("Такой файл не найден");
                    }
                    currentState = State.WAIT;
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
                    //if (receivedFileLength % 100 == 0) controller.setTfLogServer("" + receivedFileLength * 0.01);
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.WAIT;
                        controller.setTfLogServer(LOGER + "Файл получен!");
                        out.close();
                        updateFilesList(ctx, clientPath);
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
                    } else {
                        ctx.channel().close();
                        controller.setTfLogServer(LOGER + "ctx.channel().isActive()" + ctx.channel().isActive());
                    }
                    updateFilesList(ctx, clientPath);
                    controller.setTfLogServer(LOGER + " " + State.WAIT);
                    currentState = State.WAIT;
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }


    public void updateFilesList(ChannelHandlerContext ctx, Path path) {
        try {
            List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
            if (list.size() > 0) {
                Iterator iterator = list.iterator();
                StringBuilder stringListFileInfo = new StringBuilder();
                StringSender.sendSignalByte(ctx.channel(), SignalByte.UPDATE_LIST_SERVER);
                controller.setTfLogServer(SignalByte.UPDATE_LIST_SERVER.toString());
                while (iterator.hasNext()) {
                    stringListFileInfo.append(iterator.next().toString());
                }
                System.out.println(stringListFileInfo);
                controller.setTfLogServer("Отправка нового списка файлов - " + stringListFileInfo);
                StringSender.sendString(String.valueOf(stringListFileInfo), ctx.channel());
                currentState = State.WAIT;
            } else {
               StringSender.sendSignalByte(ctx.channel(), SignalByte.CLEAR_LIST_SERVER);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
