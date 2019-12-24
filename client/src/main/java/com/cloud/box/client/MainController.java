package com.cloud.box.client;

import com.cloud.box.common.AbstractMessage;
import com.cloud.box.common.FileDel;
import com.cloud.box.common.FileListSrv;
import com.cloud.box.common.FileMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

import static com.cloud.box.client.MainClient.lg;
import static com.cloud.box.client.MainClient.pw;

public class MainController implements Initializable {

    @FXML
    TextField tfFileName;

    @FXML
    TextField tfLoadFile;

    @FXML
    ListView<String> clientFilesList;

    @FXML
    ListView<String> serverFilesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()),
                                fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    if (am instanceof FileListSrv) {
                        FileListSrv fl = (FileListSrv) am;
                        Platform.runLater(() -> {
                            try {
                                serverFilesList.getItems().clear();
                                fl.getList().forEach(o -> serverFilesList.getItems().add(o));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        clientFilesList.setItems(FXCollections.observableArrayList());
        refreshLocalFilesList();
        refreshServerFilesList();
    }


    public void pressOnDownloadBtn (ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileMessage(tfFileName.getText(), lg, pw));
            tfFileName.clear();
        }
    }
    public void pressOnUploadBtn (ActionEvent actionEvent) throws IOException {
        String path = "client_storage/" + tfLoadFile.getText();
        if (tfLoadFile.getLength() > 0 && Files.exists(Paths.get(path))) {
            FileMessage fileMessage = null;
            try {
                fileMessage = new FileMessage(Paths.get(path), lg, pw);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Network.sendMsg(fileMessage);
            tfLoadFile.clear();
            refreshServerFilesList();
        }
    }


    public void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                clientFilesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> clientFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshServerFilesList() {
        updateUI(() -> {
            try {
                serverFilesList.getItems().clear();
                Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> serverFilesList.getItems().add(o));

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void delBtnServer(ActionEvent actionEvent) {
        boolean st = serverFilesList
                .getItems()
                .stream()
                .anyMatch(s -> s.equals(tfFileName.getText()));
        if (tfFileName.getLength() > 0 && st) {
            FileDel fileDel = new FileDel(tfFileName.getText(), lg, pw);
            Network.sendMsg(fileDel);
            tfFileName.clear();
            refreshServerFilesList();
        }
    }
    public void delBtnClient(ActionEvent actionEvent) {
        String path = "client_storage/" + tfLoadFile.getText();
        if (tfLoadFile.getLength() > 0 && Files.exists(Paths.get(path))) {
            try {
                Files.delete(Paths.get(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
            tfLoadFile.clear();
            refreshLocalFilesList();
        }
    }

    public void clickListServer(MouseEvent mouseEvent) {
        tfFileName.setText(serverFilesList.getFocusModel().getFocusedItem());
    }

    public void clickListClient(MouseEvent mouseEvent) {
        tfLoadFile.setText(clientFilesList.getFocusModel().getFocusedItem());
    }


    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}
