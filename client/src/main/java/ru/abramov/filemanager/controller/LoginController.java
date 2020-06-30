package ru.abramov.filemanager.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.abramov.filemanager.network.NettyClient;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    TextField tfLogin;

    @FXML
    PasswordField pfPassword;

    public static NettyClient getNettyClient() {
        return nettyClient;
    }

    private static NettyClient nettyClient;

    public void connect(ActionEvent actionEvent) throws IOException {
        //String s = "connect\n"+"Login = " + tfLogin.getText()+ ". \nPassword = "+ pfPassword.getText();
        String s = tfLogin.getText();
        System.out.println(s);
        nettyClient.sendMessage(s);
        Main.setRoot("/main");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("run fxml login");
        nettyClient = new NettyClient(args -> {
            tfLogin.setText((String)args[0]);
        });
    }


    public void registration(ActionEvent actionEvent) {
    }

    public void exit(ActionEvent actionEvent) {
        nettyClient.close();
        Platform.exit();
    }
}
