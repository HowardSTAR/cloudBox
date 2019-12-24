package com.cloud.box.client;

import com.cloud.box.common.Command;
import com.cloud.box.common.Packet2Data;
import com.cloud.box.common.Packet;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Network {
    private static Network curInstance = new Network();

    public static Network getInstance() {
        return curInstance;
    }

    private Socket socket = null;
    private ObjectDecoderInputStream is = null;
    private ObjectEncoderOutputStream os = null;

    // ** Подключение
    public boolean connect() throws IOException {
        socket = new Socket(Command.HOST, Command.PORT);
        is = new ObjectDecoderInputStream(socket.getInputStream(), Command.MAX_OBJ_SIZE);
        os = new ObjectEncoderOutputStream(socket.getOutputStream());

        return true;
    }

    public void sendData(Object data) throws IOException {
        os.writeObject(data);
        os.flush();
    }

    public Object readData() throws IOException, ClassNotFoundException {
        return is.readObject();
    }

    // ** Клиент соединился
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    // ** Закрыть сокет
    public void close() {

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ** Отправка сообщения на сервер
    public void sendMsg(Packet p, ProgressBar pb) {
        try {
            // TODO отправляется файл, для других пакетов будет null
            if (pb != null) {

                Platform.runLater(() -> {
                    pb.setVisible(true);
                    pb.setManaged(true);
                });

                Packet2Data tp = ((Packet2Data) p);
                tp.get(null); // формируем байтовый массив, null - массив формировать из файла целиком, а не бить частями

                // посмотрим на сколько частей разбить
                int parts = tp.getBytes().length / Command.MAX_OBJ_SIZE;

                if (parts <= 1) {
                    sendData(tp);
                } else {
                    // больше чем одна часть, поэтому увеличиваем на +1
                    parts++;

                    // Отправим все части большого файла
                    for (int i = 0; i < parts; i++) {

                        int start = i * Command.MAX_OBJ_SIZE;
                        int end = (i + 1) * Command.MAX_OBJ_SIZE;

                        if (end > tp.getBytes().length) {
                            end = tp.getBytes().length;
                        }

                        // отметим прогресс по этому действию
                        double pr = (double) i / parts;

                        // сформируем пакет из части байт
                        Packet2Data packetData = new Packet2Data(tp.getUsrOwner(), tp.getFile());
                        packetData.get(Arrays.copyOfRange(tp.getBytes(), start, end));

                        // отправляем его на сервер
                        sendData(packetData);

                        // рисуем прогресс
                        Platform.runLater(() -> pb.setProgress(pr));
                    }
                }
            }
            else {
                // для пакетов без прогресс бара , считаем что это не длительная и не тяжелая операция
                sendData(p);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (pb != null) {
                Platform.runLater(() -> {
                    pb.setVisible(false);
                    pb.setManaged(false);
                });
            }
        }
    }
}