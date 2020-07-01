package ru.abramov.filemanager.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import ru.abramov.filemanager.network.FileSender;
import ru.abramov.filemanager.network.NettyClient;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    VBox panelClient, panelServer;

    NettyClient nettyClient = LoginController.getNettyClient();
    private static Path serverPath;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Path serverPath = Paths.get("./", "TestA", "server");
        System.out.println(serverPath);
        PanelController serverPC = (PanelController) panelServer.getProperties().get("ctrl");
        serverPC.updateList(serverPath.normalize().toAbsolutePath());
        System.out.println("run fxml main");
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
        PanelController clientPC = (PanelController) panelClient.getProperties().get("ctrl");
        PanelController serverPC = (PanelController) panelServer.getProperties().get("ctrl");
        if (clientPC.getSelectedFileName() == null && serverPC.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Нифига не выбрано", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        PanelController srcPC = null;

        if (clientPC.getSelectedFileName() != null) {
            srcPC = clientPC;
        }
        if (serverPC.getSelectedFileName() != null) {
            srcPC = serverPC;
        }
        Path srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFileName());

        try {
            Files.delete(srcPath);
            srcPC.updateList(Paths.get(srcPC.getCurrentPath())); //обновляем панель куда скопировали
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не получилось скопировать", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void connect(ActionEvent actionEvent) {
    }

    public void menuSingOut(ActionEvent actionEvent) throws IOException {
        Main.setRoot("/login");
        nettyClient.close();
    }


    public void sendFile() {
        PanelController clientPC = (PanelController) panelClient.getProperties().get("ctrl");
        PanelController serverPC = (PanelController) panelServer.getProperties().get("ctrl");
        try {
            FileSender.sendFile(Paths.get(clientPC.getAbsolutePathSelectedFile()), nettyClient.getChannel(), future -> {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
                if (future.isSuccess()) {
                    System.out.println("Файл " + clientPC.getSelectedFileName() + " успешно передан");
                    serverPC.updateList(Paths.get(serverPC.getCurrentPath()));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось отправить файл" + serverPath.normalize().toAbsolutePath(), ButtonType.OK);
            alert.showAndWait();
        }
    }
}
