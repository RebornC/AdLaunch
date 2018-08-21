package com.example.yc.adlaunch;

/**
 * Created by yc on 2018/8/19.
 */

public class advertisement {
    private String updateTime;
    private String version;
    private String imageUrl;
    private String httpUrl;

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getVersion() {
        return version;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getHttpUrl() {
        return httpUrl;
    }
}
