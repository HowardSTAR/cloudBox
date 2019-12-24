package com.cloud.box.server;

import com.cloud.box.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private List<String> usersDir;

    public MainHandler() {
        loadUsersDir();
    }

    private void loadUsersDir(){ // Получим все каталоги пользователей , списком
        usersDir = new ArrayList<>();
        usersDir.addAll(Arrays.asList(Paths.get(Command.cloudAbsPath).toFile().list()));
    }

    private boolean createUserDir(String usr) { // Создать каталог пользователя
        File usrDir = new File(Command.cloudAbsPath + "/" + usr);
        if (!usrDir.exists()) {
            return  usrDir.mkdir();
        }
        return true;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected.");
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {

            /////////////////////////////////////////////
            // ** Пустое сообщение не обрабатывается
            /////////////////////////////////////////////
            if (msg == null)
                return;

            /////////////////////////////////////////////
            // ** Пакет с логином пользователя
            /////////////////////////////////////////////
            if (msg instanceof Packet1Login) {
                AuthDB db = new AuthDB();
                String usr = db.userLogin(((Packet1Login) msg).getToken());

                if (usr == null) {
                    // Отправляем сообщение, что не авторизован
                    System.out.println("Ошибка авторизации. Пользователь или пароль не верные.");

                    ctx.write(new Packet4Command(usr, Command.MSG_AUTH_ERR));
                    ctx.flush();

                    return;
                }

                if (!usr.isEmpty()) { // Пользователь нашелся в БД
                    // Проверим, авторизован ли он первый раз - создать каталог
                    if (!createUserDir(usr)) throw new IOException(Command.MSG_ERR_MKDIR.join(" ", usr));
                    // Отправляем сообщение, что авторизован
                    ctx.write(new Packet4Command(usr, Command.MSG_AUTH_OK + usr));
                    ctx.flush();

                    return;
                }
            }

            /////////////////////////////////////////////
            // *** Пакет с данными ***
            /////////////////////////////////////////////
            if (msg instanceof Packet2Data) {
                Packet2Data packet = ((Packet2Data) msg);

                String pathname = String.join("\\",
                        Command.cloudAbsPath,
                        packet.getUsrOwner(),
                        packet.getFile().getName());


                File f = new File(pathname);
                f.delete();
                if (!f.exists()) { f.createNewFile(); }

                // Пишем файл в каталог пользователя
                Files.write(Paths.get(pathname), ((Packet2Data) msg).getBytes(), StandardOpenOption.APPEND);

                return;
            }

            /////////////////////////////////////////////
            // *** Пакет команда ***
            /////////////////////////////////////////////
            if (msg instanceof Packet4Command) {

                // обработка команды получения файла
                Packet4Command packet = ((Packet4Command) msg);

                Packet2Data tp = new Packet2Data(packet.getUser(),
                        new File(
                                Command.cloudAbsPath +
                                        "/" +
                                        packet.getUser() +
                                        "/" +
                                        packet.getCommand()
                        ));
                tp.get(null);

                // посмотрим на сколько частей разбить
                int parts = tp.getBytes().length / Command.MAX_OBJ_SIZE;


                if (parts <= 1) {
                    ctx.writeAndFlush(tp);
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

                        // отправляем его клиенту
                        ctx.writeAndFlush(packetData);
                    }
                }
                return;
            }

            /////////////////////////////////////////////
            // ** Ошибочный пакет
            /////////////////////////////////////////////
            System.out.printf("Server received wrong object!");
        }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}