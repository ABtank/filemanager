import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    TextArea textArea;

    @FXML
    TextField msgField;

    public Network network;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            network = new Network(8189);
            new Thread(() -> {
                try {
                    while (true) {
                        String msg = network.reedMessage();
                        textArea.appendText(msg + "\n");
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "соединение разорвано", ButtonType.OK);
                        alert.show();
                    });
                } finally {
                    network.close();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("не подключились");
        }

    }


    public void menuItemFileExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }


    public void sendMsg(ActionEvent actionEvent) {
        try {
            network.sendMessage(msgField.getText()); //послали сообщение
            msgField.clear();    // очистили поле
            msgField.requestFocus();  // вернули поле
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "не отправилось", ButtonType.OK);
            alert.show();
        }
    }
}
