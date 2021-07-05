package ru.abramov.filemanager.netty.server;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component ("controller")
@FxmlView("/mainserver.fxml")
public class Controller implements Initializable {
    @FXML
    TextArea taLogServer;

    public static String log = "Welcome";

    public void connect(ActionEvent actionEvent) {
        new NettyServer(this);
        setTfLogServer("connect");
    }

    public void disconnect(ActionEvent actionEvent) {
        setTfLogServer("disconnect");
        NettyServer.close();
    }

    public void exit(ActionEvent actionEvent) {
        NettyServer.close();
        Platform.exit();
    }

    public void setTfLogServer(String log) {
        Platform.runLater(() -> taLogServer.appendText(log + "\n"));
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        taLogServer.appendText(log + "\n");
    }
}