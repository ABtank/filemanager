import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    TableView filesListClient;

    @FXML
    TableView filesListServer;

    @FXML
    TextField pathField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void menuItemFileExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void filesListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {

        }
    }

    public void copyAction(ActionEvent actionEvent) {

    }

    public void moveAction(ActionEvent actionEvent) {
    }

    public void deleteAction(ActionEvent actionEvent) {
    }

    public void connect(ActionEvent actionEvent) {
    }
}
