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
        if(!tfLogin.getText().trim().isEmpty()&&!pfPassword.getText().isEmpty()){
            nettyClient = new NettyClient(tfLogin.getText(),pfPassword.getText());
            System.out.println(nettyClient.getChannel()+" "+tfLogin.getText()+" "+pfPassword.getText());
            Main.setRoot("/main");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        System.out.println("run fxml login");

    }


    public void registration(ActionEvent actionEvent) {
    }

    public void exit(ActionEvent actionEvent) {
        nettyClient.close();
        Platform.exit();
    }
}
