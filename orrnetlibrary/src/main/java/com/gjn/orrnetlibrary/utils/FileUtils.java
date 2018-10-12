package com.gjn.orrnetlibrary.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * @author gjn
 * @time 2018/10/9 15:43
 */

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static String getSuffix(File file) {
        return file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }

    public static String getTypeFromSuffix(File file) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getSuffix(file));
    }

    public static void openFile(Context context, String filePath) {
        File file = new File(filePath);
        openFile(context, file);
    }

    public static void openFile(Context context, File file) {
        String mimeType = getTypeFromSuffix(file);
        Uri uri = Uri.fromFile(file);
        if (mimeType != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, mimeType);
            context.startActivity(intent);
        }
    }

    public static void deleteFile(String directoryPath) {
        File directory = new File(directoryPath);
        deleteFile(directory);
    }

    public static void deleteFile(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            String[] children = directory.list();
            for (String child : children) {
                deleteFile(new File(directory, child));
            }
        }
        directory.delete();
    }

    public static long getFileSize(String directoryPath) {
        File directory = new File(directoryPath);
        return getFileSize(directory);
    }

    public static long getFileSize(File directory) {
        long size = 0;
        try {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    size += getFileSize(file);
                } else {
                    size += file.length();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "get file size error", e);
        }
        return size;
    }
}
