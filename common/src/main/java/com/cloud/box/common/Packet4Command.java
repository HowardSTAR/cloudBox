package com.cloud.box.common;

public class Packet4Command extends Packet {

    private static final long serialVersionUID = -1841385066417334642L;

    private String user;
    private String command;

    public Packet4Command(String user, String command) {
        this.user = user;
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public String getUser() {
        return user;
    }
}
