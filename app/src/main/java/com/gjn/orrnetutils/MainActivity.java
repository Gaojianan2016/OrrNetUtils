package com.gjn.orrnetutils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gjn.orrnetlibrary.DownLoadManager;
import com.gjn.orrnetlibrary.OkHttpManager;
import com.gjn.orrnetlibrary.RetrofitManager;
import com.gjn.permissionlibrary.PermissionCallBack;
import com.gjn.permissionlibrary.PermissionUtils;
import com.google.gson.Gson;

import java.io.IOException;

import io.reactivex.functions.Consumer;
import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    TextView textView;

    String img = "https://www.baidu.com/img/superlogo_c4d7df0a003d3db9b65e9ef0fe6da1ec.png?where=super";

    String apk = "http://static.gumiss.com/upload/apk/shoumi_latest.apk";

    String gank = "https://gank.io/api/data/福利/10/1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionUtils.requestPermissions(this, PermissionUtils.CODE_STORAGE,
                PermissionUtils.CODE_PHONE);

        textView = findViewById(R.id.textView);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("-s-", "button");
                DownLoadManager.getInstance().download(apk, new DownLoadManager.OnDownLoadListener() {
                    @Override
                    public void start(long allSize) {
                        write("文件大小 = " + allSize);
                    }

                    @Override
                    public void error(IOException e) {
                        add("下载错误: " + e.getMessage());
                    }

                    @Override
                    public void success() {
                        add("下载成功");
                    }

                    @Override
                    public void fail() {
                        add("下载失败");
                    }

                    @Override
                    public void download(long size) {
                        add("下载中 = " + size);
                    }
                });
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("-s-", "button2");
                OkHttpManager.getInstance().get(gank, new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        write("OkHttp连接失败" + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String str = response.body().string();
                        write("OkHttp连接成功\n" + str);
                    }
                });
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("-s-", "button3");
                RetrofitManager.linkOnMainThread(RetrofitManager.create(IApi.BASE_URL, IApi.class).fuli(1, 10),
                        new Consumer<GankResponse>() {
                            @Override
                            public void accept(GankResponse gankResponse) throws Exception {
                                write("Retrofit连接成功\n" + new Gson().toJson(gankResponse));
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                write("Retrofit连接失败" + throwable.getMessage());
                            }
                        });
            }
        });
    }

    private void write(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(s);
            }
        });
    }

    private void add(String s) {
        write(textView.getText().toString() + "\n" + s);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, new PermissionCallBack() {
            @Override
            public void onSuccess(int i) {

            }

            @Override
            public void onFail(int i) {

            }

            @Override
            public void onRetry(int i) {

            }

            @Override
            public void onSetting(int i) {

            }
        });
    }
}
