package com.example.yc.adlaunch;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import retrofit2.Response;


/**
 * Created by yc on 2018/8/19.
 * 此方法用于更新最新的广告版本
 */

public class updateAdvertisement {

    private advertisement latestAd;
    private String currentVersion;
    private String latestVersion;
    private String imageUrl;

    private static final int LOAD_SUCCESS = 1;// 加载成功
    private static final int LOAD_ERROR = -1;// 加载失败
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 下载成功
                case LOAD_SUCCESS:
                    Log.i("LOAD_SUCCESS", "图片下载成功");
                    break;
                // 下载失败
                case LOAD_ERROR:
                    Log.i("LOAD_ERROR", "图片下载失败");
                    break;
            }
        }
    };

    public void getLatestVersion(Context context, Response<List<advertisement>> response) {

        // 先获取当前广告版本，0表示尚未植入广告
        currentVersion = sharedPreferencesUtil.get(context, "version", "0").toString();
        Log.i("currentVersion", currentVersion);

        if (response.body().size() != 0) {
            latestAd = response.body().get(response.body().size()-1);
            latestVersion = latestAd.getVersion();
            Log.i("latestVersion", latestVersion);

            // 如果发现版本不同，即进行更新
            if (!latestVersion.equals(currentVersion)) {
                sharedPreferencesUtil.put(context, "updateTime", latestAd.getUpdateTime());
                sharedPreferencesUtil.put(context, "version", latestAd.getVersion());
                sharedPreferencesUtil.put(context, "imageUrl", latestAd.getImageUrl());
                sharedPreferencesUtil.put(context, "httpUrl", latestAd.getHttpUrl());
                imageUrl = latestAd.getImageUrl();
                // 开启子线程从网络下载图片
                new Thread(new Runnable() {
                    public void run() {
                        getPicture();
                    }
                }).start();
            }
        }

        return;
    }

    // 下载图片的主方法
    private void getPicture() {
        URL url = null;
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            // 构建图片的url地址
            url = new URL(imageUrl);
            // 开启连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置超时的时间，5000毫秒即5秒
            conn.setConnectTimeout(5000);
            // 设置获取图片的方式为GET
            conn.setRequestMethod("GET");
            // 响应码为200，则访问成功
            if (conn.getResponseCode() == 200) {
                // 获取连接的输入流，这个输入流就是图片的输入流
                is = conn.getInputStream();
                // 构建一个file对象用于存储图片
                File file = new File(Environment.getExternalStorageDirectory(), "adImage.jpg");
                fos = new FileOutputStream(file);
                int len = 0;
                byte[] buffer = new byte[1024];
                // 将输入流写入到我们定义好的文件中
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                // 将缓冲刷入文件
                fos.flush();
                // 告诉handler，图片下载成功
                handler.sendEmptyMessage(LOAD_SUCCESS);

            }
        } catch (Exception e) {
            // 告诉handler，图片下载失败
            handler.sendEmptyMessage(LOAD_ERROR);
            e.printStackTrace();
        } finally {
            // 最后，将各种流关闭
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                handler.sendEmptyMessage(LOAD_ERROR);
                e.printStackTrace();
            }
        }
    }

}
