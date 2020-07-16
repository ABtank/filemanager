package ru.abramov.filemanager.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.abramov.filemanager.common.FileInfo;
import ru.abramov.filemanager.controller.Controller;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ByteProtocolClientHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        WAIT, NAME_FILE_LENGTH, NAME, FILE_LENGTH, FILE, NICKNAME_LENGTH, NICKNAME, FILE_LIST_LENGTH, FILELIST
    }

    public enum SignalByte {
        GET_FILE((byte) 32), AUTH((byte) 22),
        UPDATE_LIST_SERVER((byte)321);
        private byte act;

        SignalByte(byte act) {
            this.act = act;
        }

        public byte getActByte() {
            return act;
        }
    }

    private static Controller controller;
    private static final String LOGER = "LOGER ByteProtocolClientHandler: ";
    private State currentState = State.WAIT;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private Path clientPath = Paths.get("./");


    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public static void setController(Controller controller) {
        ByteProtocolClientHandler.controller = controller;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(LOGER + "Client connect :" + ctx);
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
                    System.out.println(LOGER + "Сигнальный байт =" + signalByte + " = копирование файла");
                } else if (signalByte == SignalByte.AUTH.getActByte()) {
                    currentState = State.NICKNAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println(LOGER + "Сигнальный байт = " + signalByte + " = авторизация");
                } else if (signalByte == SignalByte.UPDATE_LIST_SERVER.getActByte()) {
                    System.out.println(SignalByte.UPDATE_LIST_SERVER.toString());
                    currentState = State.FILE_LIST_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println(LOGER + "Сигнальный байт = " + signalByte + " = добавить в список сервера FileInfo");
                } else {
                    System.out.println(LOGER + "Invalid first byte - " + signalByte);
                }
            }

//            Получаем длинну nickname
            getLengthNickname(buf);
//            Получаем nickname
            getNickname(buf);

//            Получаем длинну слроки
            getLengthFileList(buf);
//            Получаем строку
            getFileInfoToList(buf);
//            Получаем файл с сервера
            getFileFromServer(buf);

        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void getFileFromServer(ByteBuf buf) throws IOException {
        //            Получаем длинну имени файла
        if (currentState == State.NAME_FILE_LENGTH) {
            if (buf.readableBytes() >= 4) {
                System.out.println(LOGER + "Получаем длинну имени файла");
                nextLength = buf.readInt();
                System.out.println("" + nextLength);
                currentState = State.NAME;
            }
        }
//              Получаем имя файла и открываем поток прописывая новое имя файла
        if (currentState == State.NAME) {
            if (buf.readableBytes() >= nextLength) {
                byte[] fileNameByte = new byte[nextLength];
                buf.readBytes(fileNameByte);
                String fileName = new String(fileNameByte, "UTF-8");
                System.out.println(LOGER + "Получаем имя файла и открываем поток - " + fileName);
                clientPath = Controller.getDestination();
                System.out.println(clientPath + fileName);
                out = new BufferedOutputStream(new FileOutputStream(clientPath +"\\"+ fileName));
                currentState = State.FILE_LENGTH;
            }
        }

//            Получаем длинну файла
        if (currentState == State.FILE_LENGTH) {
            if (buf.readableBytes() >= 8) {
                fileLength = buf.readLong();
                System.out.println(LOGER + "Получаем длинну файла - " + fileLength);
                currentState = State.FILE;
            }
        }
//              записываем файл
        if (currentState == State.FILE) {
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLength++;
                if (fileLength == receivedFileLength) {
                    currentState = State.WAIT;
                    System.out.println(LOGER + "Файл получен!");
                    out.close();
                    break;
                }
            }
        }
    }

    private void getLengthNickname(ByteBuf buf) {
        if (currentState == State.NICKNAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                System.out.println(LOGER + "Получаем длинну nickname");
                nextLength = buf.readInt();
                System.out.println(LOGER + "Длинна nickname = " + nextLength);
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
                System.out.println(LOGER + "Получаем nickname =" + nickname);
                Controller.setNickname(nickname);
                currentState = State.WAIT;
            }
        }
    }

    private void getLengthFileList(ByteBuf buf) {
        if (currentState == State.FILE_LIST_LENGTH) {
            if (buf.readableBytes() >= 4) {
                System.out.println(LOGER + "Получаем длинну FILE_LIST_LENGTH");
                nextLength = buf.readInt();
                System.out.println(LOGER + "Длинна FILE_LIST_LENGTH = " + nextLength);
                if(nextLength == 0) currentState = State.WAIT;
                currentState = State.FILELIST;
            }
        }
    }

    private void getFileInfoToList(ByteBuf buf) throws UnsupportedEncodingException {
        if (currentState == State.FILELIST) {
            if (buf.readableBytes() >= nextLength) {
                byte[] ListFileInfoByte = new byte[nextLength];
                buf.readBytes(ListFileInfoByte);
                String fileInfoString = new String(ListFileInfoByte, "UTF-8");
                String[]strArr = fileInfoString.split("\n");
                List<FileInfo> fileInfoList = new ArrayList<>();
                for (int i = 0; i <strArr.length ; i++) {
                    String arr[]=strArr[i].split(" ");
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileName(arr[0]);
                    fileInfo.setSize(Long.parseLong(arr[1]));
                    fileInfoList.add(fileInfo);
                }
                System.out.println(LOGER + "Получаем FILEINFOLIST =" + fileInfoList);
                controller.serverListUpdate(fileInfoList);
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
