package ru.abramov.filemanager.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ru.abramov.filemanager.controller.PanelController;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ByteProtocolHandler extends ChannelInboundHandlerAdapter {
    @FXML
    Label lbNickname;

    @FXML
    VBox panelServer;

    public enum State {
        WAIT, NAME_LENGTH, NAME, FILE_LENGTH, FILE, NICKNAME_LENGTH, NICKNAME
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

    private static final String CLIENT = "CLIENT: ";
    private State currentState = State.WAIT;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private Path clientPath;


    private String nickname;

    public String getNickname() {
        return nickname;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(CLIENT + "Client connect :" + ctx);
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
                    System.out.println(CLIENT + "Сигнальный байт =" + readed + " = копирование файла");
                } else if (readed == Act.AUTH.getActByte()) {
                    currentState = State.NICKNAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println(CLIENT + "Сигнальный байт = " + readed + " = авторизация");
                } else {
                    System.out.println(CLIENT + "Invalid first byte - " + readed);
                }
            }

//            Получаем длинну nickname
            if (currentState == State.NICKNAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println(CLIENT + "Получаем длинну nickname");
                    nextLength = buf.readInt();
                    System.out.println(CLIENT + "Длинна nickname = " + nextLength);
                    currentState = State.NICKNAME;
                }
            }
//            Получаем nickname
            getNickname(buf);
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void getNickname(ByteBuf buf) throws UnsupportedEncodingException {
        if (currentState == State.NICKNAME) {
            if (buf.readableBytes() >= nextLength) {
                byte[] clientLoginBuf = new byte[nextLength];
                buf.readBytes(clientLoginBuf);
                nickname = new String(clientLoginBuf, "UTF-8");
                System.out.println(CLIENT + "Получаем nickname =" + nickname);
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
