import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    TextField tfLogin;

    @FXML
    PasswordField pfPassword;

    public void connect(ActionEvent actionEvent) throws IOException {
        System.out.println("connect\n"+"Login = " + tfLogin.getText()+ ". \nPassword = "+ pfPassword.getText());
        Main.setRoot("main");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("fxml login");
    }
}
