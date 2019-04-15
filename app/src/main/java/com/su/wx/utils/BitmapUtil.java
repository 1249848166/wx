package com.su.wx.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapUtil {

    public interface Callback {
        void forDone();
    }

    public static void encode(final String savePath, final String filePath, final Callback callback) throws IOException {
        ThreadUtil.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bm = BitmapFactory.decodeFile(filePath);
                    if (getBitmapSize(bm) / 1000 > 700) {
                        Log.e("aaa", "大于700k," + (getBitmapSize(bm) / 1000));
                        BitmapFactory.Options options1 = new BitmapFactory.Options();
                        options1.inSampleSize = 2;
                        options1.inPreferredConfig = Bitmap.Config.RGB_565;
                        Bitmap bm1 = BitmapFactory.decodeFile(filePath, options1);
                        File dir = new File(Environment.getExternalStorageDirectory()+Storage.cacheDirPath);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        FileOutputStream fout = new FileOutputStream(savePath);
                        bm1.compress(Bitmap.CompressFormat.JPEG, 30, fout);
                        fout.close();
                    } else {
                        Log.e("bbb", "足够小");
                        Bitmap bm1 = BitmapFactory.decodeFile(filePath);
                        File dir = new File(Environment.getExternalStorageDirectory()+Storage.cacheDirPath);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        FileOutputStream fout = new FileOutputStream(savePath);
                        bm1.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                        fout.close();
                    }
                    if (callback != null) {
                        callback.forDone();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {     //API 19
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight(); //earlier version
        }
    }

}
