package com.cloud.box.common;

public class Packet1Login extends Packet {

    private static final long serialVersionUID = -7432971959150922452L;
    private String login;
    private String password;
    private String[] token;

    public Packet1Login(String login, String password) {
        // Родительские поля класса
        this.login = login;
        this.password = password;
        this.token = new String[] {this.login, this.password};
    }

    public String[] getToken() {
        return token;
    }
}