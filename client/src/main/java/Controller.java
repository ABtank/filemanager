import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import javax.swing.text.TabableView;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class Controller implements Initializable {

    @FXML
    VBox panelClient, panelServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new NIOClient();
    }


    public void menuItemFileExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void filesListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {

        }
    }

    public void copyAction(ActionEvent actionEvent) {
        PanelController clientPC = (PanelController) panelClient.getProperties().get("ctrl");
        PanelController serverPC = (PanelController) panelServer.getProperties().get("ctrl");

        if (clientPC.getSelectedFileName() == null && serverPC.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Нифига не выбрано", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        // определяемся с направлением копирования
        PanelController srcPC = null, dstPC = null;

        if (clientPC.getSelectedFileName() != null) {
            srcPC = clientPC;
            dstPC = serverPC;
        }
        if (serverPC.getSelectedFileName() != null) {
            srcPC = serverPC;
            dstPC = clientPC;
        }
        // получаем пути для копирования
        Path srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFileName());
        Path dstPath = Paths.get(dstPC.getCurrentPath()).resolve(srcPath.getFileName().toString());

        try {
            Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
            dstPC.updateList(Paths.get(dstPC.getCurrentPath())); //обновляем панель куда скопировали
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не получилось скопировать", ButtonType.OK);
            alert.showAndWait();
        }

    }

    public void moveAction(ActionEvent actionEvent) {
        PanelController clientPC = (PanelController) panelClient.getProperties().get("ctrl");
        PanelController serverPC = (PanelController) panelServer.getProperties().get("ctrl");

        if (clientPC.getSelectedFileName() == null && serverPC.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Нифига не выбрано", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        // определяемся с направлением копирования
        PanelController srcPC = null, dstPC = null;

        if (clientPC.getSelectedFileName() != null) {
            srcPC = clientPC;
            dstPC = serverPC;
        }
        if (serverPC.getSelectedFileName() != null) {
            srcPC = serverPC;
            dstPC = clientPC;
        }
        // получаем пути для копирования
        Path srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFileName());
        Path dstPath = Paths.get(dstPC.getCurrentPath()).resolve(srcPath.getFileName().toString());

        try {
            Files.move(srcPath, dstPath);
            dstPC.updateList(Paths.get(dstPC.getCurrentPath())); //обновляем панель куда скопировали
            srcPC.updateList(Paths.get(srcPC.getCurrentPath()));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не получилось скопировать", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void deleteAction(ActionEvent actionEvent) {
    }

    public void connect(ActionEvent actionEvent) {
    }
}
