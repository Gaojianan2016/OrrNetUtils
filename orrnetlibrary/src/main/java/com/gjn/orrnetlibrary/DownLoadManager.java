package com.gjn.orrnetlibrary;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.gjn.orrnetlibrary.utils.OkHttpClientFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    private static OkHttpClient okHttpClient;
    private static DownLoadManager downLoadManager;

    public DownLoadManager(Interceptor it) {
        if (it == null) {
            it = new DefaultInterceptor();
        }
        if (okHttpClient == null) {
            okHttpClient = OkHttpClientFactory.create(it);
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
        String path = "/sdcard/";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getPath() + "/";
        }
        download(url, path, listener);
    }

    public void download(String url, String path, final OnDownLoadListener listener) {
        download(url, path, "", listener);
    }

    public void download(String url, String path, String name, final OnDownLoadListener listener) {
        listener.startBefore();
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
        okHttpClient.newCall(new Request.Builder().url(url).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        listener.fail();
                        listener.error(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (downloadStream(response, file, listener)) {
                            listener.success();
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
        if (allSize > 1024 * 500) {
            return 1024 * 20;
        } else if (allSize > 1024 * 100) {
            return 1024 * 10;
        } else if (allSize > 1024 * 50) {
            return 1024 * 5;
        } else if (allSize > 1024 * 10) {
            return 1024 * 2;
        }
        return 1024;
    }

    public static abstract class OnDownLoadListener{
        public void startBefore(){}

        public void start(long allSize){}

        public void error(IOException e){}

        public void success(){}

        public void fail(){};

        public void download(long size){}
    }
}
