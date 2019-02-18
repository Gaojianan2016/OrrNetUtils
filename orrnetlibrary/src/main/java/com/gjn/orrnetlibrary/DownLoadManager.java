package com.gjn.orrnetlibrary;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author gjn
 * @time 2018/10/9 16:31
 */

public class DownLoadManager {
    private static final String TAG = "DownLoadManager";

    public static final String SDCARD = "/sdcard/";

    public static int XS = 5;

    private static OkHttpClient okHttpClient;
    private static DownLoadManager downLoadManager;

    public DownLoadManager(Interceptor it) {
        if (it == null) {
            it = new DefaultInterceptor();
        }
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();
        }
    }

    public static DownLoadManager getInstance() {
        if (downLoadManager == null) {
            synchronized (OkHttpManager.class) {
                if (downLoadManager == null) {
                    downLoadManager = new DownLoadManager(null);
                }
            }
        }
        return downLoadManager;
    }

    public static DownLoadManager getInstance(Interceptor interceptor) {
        if (downLoadManager == null) {
            synchronized (OkHttpManager.class) {
                if (downLoadManager == null) {
                    downLoadManager = new DownLoadManager(interceptor);
                }
            }
        }
        return downLoadManager;
    }

    public static void setOkHttpClient(OkHttpClient client) {
        okHttpClient = client;
    }

    public void download(String url, final OnDownLoadListener listener) {
        String path = SDCARD;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getPath() + "/";
        }
        download(url, path, listener);
    }

    public void download(String url, String path, final OnDownLoadListener listener) {
        download(url, path, "", listener);
    }

    public void download(String url, String path, String name, final OnDownLoadListener listener) {
        Uri uri = Uri.parse(url);
        if (name == null || name.isEmpty()) {
            name = uri.getLastPathSegment();
        }
        final File filePath = new File(path);
        final File file = new File(path, name);
        try {
            if (!filePath.exists()) {
                filePath.mkdirs();
                Log.d(TAG, "create filePath " + filePath.getPath());
            }
            if (file.exists()) {
                if (file.delete()) {
                    Log.d(TAG, "delete old file " + file.getName());
                } else {
                    Log.d(TAG, "delete old file error");
                }
            }
            file.createNewFile();
            Log.d(TAG, "create new file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        listener.startBefore(name, file);
        Log.d(TAG, "linkStart -> " + url);
        okHttpClient.newCall(new Request.Builder().url(url).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "linkFailure...");
                        listener.fail();
                        listener.error(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d(TAG, "linkResponse...");
                        if (downloadStream(response, file, listener)) {
                            listener.success(file);
                        } else {
                            listener.fail();
                        }
                    }
                });
    }

    private boolean downloadStream(Response response, File file, OnDownLoadListener listener) {
        int allSize = Integer.parseInt(response.header("Content-Length"));
        InputStream is = null;
        byte[] buf = new byte[getDownLoadLen(allSize)];
        int len;
        int size = 0;
        FileOutputStream fos = null;
        listener.start(allSize);
        try {
            Log.d(TAG, "start write file.");
            is = response.body().byteStream();
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                size += len;
                listener.download(size);
                fos.write(buf, 0, len);
            }
            fos.flush();
            Log.d(TAG, "write file success.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "write file error.");
        return false;
    }

    private int getDownLoadLen(int allSize) {
        int len = 1024;
        if (allSize > 1024 * 500) {
            len = 1024 * 20;
        } else if (allSize > 1024 * 100) {
            len = 1024 * 10;
        } else if (allSize > 1024 * 50) {
            len = 1024 * 5;
        } else if (allSize > 1024 * 10) {
            len = 1024 * 2;
        }
        return len * XS;
    }

    public static abstract class OnDownLoadListener{
        public void startBefore(String name, File file){}

        public void start(long allSize){}

        public void error(IOException e){}

        public void success(File file){}

        public void fail(){};

        public void download(long size){}
    }
}
