package com.zmq.utils;

/**
 * Created by IntelliJ IDEA.
 * User: zmq
 * Date: 2020/6/14
 */
public class Image {
    private String fullPath;
    private String thumbPath;

    public Image(String fullPath, String thumbPath) {
        this.fullPath = fullPath;
        this.thumbPath = thumbPath;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }
}
