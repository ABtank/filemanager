package ru.abramov.filemanager.netty.server;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextArea taLogServer;

    public static String log = "hi";

    public void connect(ActionEvent actionEvent) {
        new NettyServer(this);
        setTfLogServer("connect");
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void setTfLogServer(String log){
        taLogServer.appendText(log+"\n");
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        taLogServer.appendText(log+"\n");
    }
}