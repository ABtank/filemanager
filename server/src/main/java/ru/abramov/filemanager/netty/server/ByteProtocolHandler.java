package ru.abramov.filemanager.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class ByteProtocolHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        WAIT, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private String stigma = "server_";
    private State currentState = State.WAIT;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private  String nettyDir = NettyServer.getServerPath().toAbsolutePath().toString();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);

        while (buf.readableBytes() > 0) {
//            Сигнальный байт
            if (currentState == State.WAIT) {
                byte readed = buf.readByte();
                if (readed == (byte) 32) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("Сигнальный байт = (32)= копирование файла");
                } else {
                    System.out.println("Invalid first byte - " + readed);
                }
            }
//              Получаем длинну имени файла
            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("Получаем длинну имени файла");
                    nextLength = buf.readInt();
                    currentState = State.NAME;
                }
            }
//              Получаем имя файла и открываем поток прописывая новое имя файла
            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    System.out.println("Получаем имя файла и открываем поток прописывая новое имя файла - " + new String(fileName, "UTF-8"));
                    out = new BufferedOutputStream(new FileOutputStream(nettyDir+"\\"+stigma + new String(fileName)));
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
