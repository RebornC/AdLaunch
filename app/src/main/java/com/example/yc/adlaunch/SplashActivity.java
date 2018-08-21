package com.example.yc.adlaunch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yc on 2018/8/19.
 */

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView ad_image;
    private ImageView bottom_image;
    private Button ad_timer;
    private Integer ms = 4;
    private CountDownTimer textTimer;
    private Intent it;
    String httpUrl;
    // 针对SD卡读取权限申请
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.splash_activity);

        ad_image = (ImageView) findViewById(R.id.ad_image);
        ad_timer = (Button) findViewById(R.id.ad_timer);
        bottom_image = (ImageView) findViewById(R.id.bottom_image);
        ad_image.setOnClickListener(this);
        ad_timer.setOnClickListener(this);
        bottom_image.setOnClickListener(this);
        httpUrl = sharedPreferencesUtil.get(SplashActivity.this, "httpUrl", "none").toString();


        // 检查当前是否存在广告图，如果存在，则直接从本地文件中读取，否则加载默认图片
        File PicFile = new File(Environment.getExternalStorageDirectory(), "adImage.jpg");
        if (PicFile.exists()) {
            // 加载本地图片的两种方式
            ad_image.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+"/adImage.jpg"));
            //ad_image.setImageURI(Uri.fromFile(PicFile));
        } else {
            // 可在此加载默认图片
            //ad_image.setImageResource(R.mipmap.ad_test_1);
            sharedPreferencesUtil.put(SplashActivity.this, "version", "0");
        }

        verifyStoragePermissions(SplashActivity.this); //申请SD卡读写权限
        adCountDown(); //广告倒计时
        verifyNetwork(); //检查网络
    }

    /**
     * 点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ad_image:
                // 如果存在广告链接，则跳转到相应的webview界面
                if (!httpUrl.equals("none")) {
                    textTimer.cancel();
                    it = new Intent(SplashActivity.this, WebViewActivity.class);
                    it.putExtra("httpUrl", httpUrl);
                    startActivity(it);
                }
                break;
            case R.id.ad_timer:
                textTimer.cancel();
                goNextActivity();
                break;
            case R.id.bottom_image:
                textTimer.cancel();
                goNextActivity();
                break;
        }
    }

    /**
     * 针对安卓6.0以上机型的动态权限获取
     */
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有SD卡读写权限
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                //如果没有权限，则申请权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 广告倒计时
     */
    public void adCountDown() {
        textTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { //第一个参数为总计时，第二个参数为计时速度
                ms -= 1;
                ad_timer.setText("跳过 " + ms + " s");
            }

            @Override
            public void onFinish() {
                goNextActivity();
            }
        }.start();
    }

    /**
     * 先判断是否有可用网络
     * 使用ConnectivityManager获取手机所有连接管理对象
     * 使用manager获取NetworkInfo对象
     * 最后判断当前网络状态是否为连接状态即可
     */
    public void verifyNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if ((networkInfo == null) || !networkInfo.isConnected()) {
            Toast.makeText(SplashActivity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Log.i("当前网络可用", networkInfo.toString());
            // 网络获取广告资源
            getAdMsg();
        }
    }

    public void goNextActivity() {
        it = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(it);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void getAdMsg() {
        OkHttpClient client = new OkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://www.easy-mock.com/mock/5b71b3caebd4a208cce29bf1/advertisement/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        adInterface ad = retrofit.create(adInterface.class);
        Call<List<advertisement>> adCall = ad.getAdMsg();
        adCall.enqueue(new Callback<List<advertisement>>() {
            @Override
            public void onResponse(Call<List<advertisement>> call, Response<List<advertisement>> response) {
                Log.i("getAdMsg successfully", response.body().toString());
                //获取最新广告版本
                updateAdvertisement ad = new updateAdvertisement();
                ad.getLatestVersion(SplashActivity.this, response);
            }

            @Override
            public void onFailure(Call<List<advertisement>> call, Throwable t) {
                Log.i("getAdMsg fail", " " + t);
            }
        });
    }

}
