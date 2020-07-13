package ru.abramov.filemanager.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import ru.abramov.filemanager.common.FileSender;
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
    @FXML
    Label lbNickname;

    NettyClient nettyClient = LoginController.getNettyClient();
    private static Path serverPath;
    private static String nickname;

    public static void setNickname(String nickname) {
        Controller.nickname = nickname;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //1)Получаю никнейм и зааписываю в
        int i = 0;
        while (nickname == null) {
            System.out.println(nickname + i++);
            if (i >= 1000000) {
                i = 0;
                errorConnect();
                break;
            }
        }
        lbNickname.setText("Hello " + nickname);
        Path serverPath = Paths.get("./", nickname);
        System.out.println(serverPath);
        PanelController serverPC = (PanelController) panelServer.getProperties().get("ctrl");
        serverPC.updateList(serverPath.normalize().toAbsolutePath());
        System.out.println("run fxml main");
    }


    public void menuItemFileExitAction(ActionEvent actionEvent) {
        nettyClient.close();
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
            if (!Files.isDirectory(srcPath)) Files.delete(srcPath);
            srcPC.updateList(Paths.get(srcPC.getCurrentPath())); //обновляем панель куда скопировали
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Не получилось удалить", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void menuSingOut(ActionEvent actionEvent) throws IOException {
        nickname = null;
        Main.setRoot("/login");
        nettyClient.close();
    }

    public void errorConnect() {
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Нет ответа от сервера при авторизации. Либо неверно введен login/password", ButtonType.OK);
            alert.showAndWait();
            Main.setRoot("/login");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось восстановить меню авторизации", ButtonType.OK);
            alert.showAndWait();
        }
        nettyClient.close();
    }


    public void sendFile() {
        PanelController clientPC = (PanelController) panelClient.getProperties().get("ctrl");
        PanelController serverPC = (PanelController) panelServer.getProperties().get("ctrl");
        try {
            // обработка завершения передачи файла через листнер future
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
