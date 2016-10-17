package com.duke.zxing.client.android.utils;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.duke.zxing.client.android.decode.BitmapDecoder;
import com.duke.zxing.client.android.decode.QRCodeEncoder;
import com.google.zxing.Result;
import com.google.zxing.WriterException;

/**
 * Created by meitu on 2016/10/17.
 */

public class CommonUtil {

    private static BitmapDecoder sBitmapDecoder;

    public static BitmapDecoder getBitmapDecoder() {
        if (sBitmapDecoder == null) {
            sBitmapDecoder = new BitmapDecoder();
        }
        return sBitmapDecoder;
    }

    public static Result decodeByBitmap(Bitmap bitmap) {
        return getBitmapDecoder().getRawResult(bitmap);
    }

    public static Result decodeByPath(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 200);

        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        Bitmap scanBitmap = BitmapFactory.decodeFile(imagePath, options);
        return decodeByBitmap(scanBitmap);
    }

    public static Bitmap encodeStr2Bitmap(String contents, int dimension)
        throws WriterException {
        return new QRCodeEncoder(contents, dimension).encodeAsBitmap();
    }

    public static byte[] encodeStr2Bytes(String contents, int dimension)
        throws WriterException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap = encodeStr2Bitmap(contents, dimension);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

}
