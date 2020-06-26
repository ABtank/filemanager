import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelController implements Initializable {

    @FXML
    TableView<FileInfo> filesTable;

    @FXML
    ComboBox<String> diskBox;

    @FXML
    TextField pathField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>("Type");
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName())); // D или F
        fileTypeColumn.setPrefWidth(60); //базовая длинна

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName())); // D или F
        fileNameColumn.setPrefWidth(220); //базовая длинна

        // ---отображать размер файла ----

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(120);
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        // ---посл модификации ---

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Date change");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf))); // D или F
        fileDateColumn.setPrefWidth(120); //базовая длинна

        filesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);

        filesTable.getSortOrder().add(fileTypeColumn); // сортировка

        // ---Получаем список дисков ---
        diskBox.getItems().clear(); // очищаем
        for (Path p : FileSystems.getDefault().getRootDirectories()){  // заполняем
            diskBox.getItems().add(p.toString());
        }
        diskBox.getSelectionModel().select(0); // по умолчанию ставим первый

        // ---переходы по папкам в таблице ---

        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() == 2){
                    Path path = Paths.get(pathField.getText()).resolve(filesTable.getSelectionModel().getSelectedItem().getFileName());
                    if(Files.isDirectory(path)){
                        updateList(path);
                    }
                }
            }
        });

        updateList(Paths.get(".")); // корневой каталог

    }

    public void updateList(Path path) {
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            filesTable.getItems().clear(); // сначала чистим список
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Список файлов не получается обновить", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if(upperPath!= null){
            updateList(upperPath);
        }
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFileName () {
        if(!filesTable.isFocused()){ //проверка ткакая выбрана
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath() {
        return pathField.getText();
    }
}
