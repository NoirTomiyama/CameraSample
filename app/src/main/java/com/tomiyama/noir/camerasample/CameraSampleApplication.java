package com.tomiyama.noir.camerasample;

import android.app.Application;

public class CameraSampleApplication extends Application {

    private byte[] tempRawPicture = null;

    public byte[] getTempRawPicture() {
        return tempRawPicture;
    }

    public void setTempRawPicture(byte[] tempRawPicture) {
        this.tempRawPicture = tempRawPicture;
    }

    private String path = "";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
