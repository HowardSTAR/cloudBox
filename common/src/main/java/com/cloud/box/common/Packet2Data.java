package com.cloud.box.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Packet2Data extends Packet {

    private static final long serialVersionUID = 4733800969403743265L;
    private String usrOwner;
    private File file;
    private byte[] bytes;

    public Packet2Data(String usrOwner, File file) {
        this.usrOwner = usrOwner;
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public String getUsrOwner() {
        return this.usrOwner;
    }

    public void get(byte[] b) {
        try {
            if (b != null) { this.bytes = b; }
            else{

               // byte[] fileArray =
                //        org.apache.commons.io.FileUtils.readFileToByteArray(new File("obj.data"));
                this.bytes = Files.readAllBytes(file.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
