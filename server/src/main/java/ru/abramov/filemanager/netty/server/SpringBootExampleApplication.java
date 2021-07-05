package ru.abramov.filemanager.netty.server;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootExampleApplication {

    public static void main(String[] args) {
        Application.launch(ServerGUI.class, args);
    }
}
