package com.cloud.box.common;

public class Command {

    // ** Сервер
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 8179;

    // ** Клиент
    public static final String HOST = "localhost";
    public static final int PORT = 8179;

    // ** Размер буфера передачи
    public static final int MAX_OBJ_SIZE = 1024 * 1024 * 100;


    // ** Каталоги
    public static final String localAbsPath = "home\\localoutcome";
    public static final String cloudAbsPath = "home\\cloudincome";

    // ** Служебные комманды
    public static final String MSG_AUTH_ERR = "autherr ";
    public static final String MSG_AUTH_OK = "authok ";
    public static final String MSG_ERR_MKDIR = "Не могу создать каталог пользователя ";
}