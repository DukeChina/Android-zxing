package com.duke.zxing.client.android.decode;

import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

/**
 * 从bitmap解码
 * 
 * @author hugo
 * 
 */
public class BitmapDecoder {

    MultiFormatReader multiFormatReader;
    static Vector<BarcodeFormat> decodeFormats;

    public BitmapDecoder() {

        multiFormatReader = new MultiFormatReader();

        // 解码的参数
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(2);
        // 可以解析的编码类型

        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = new Vector<BarcodeFormat>();
            decodeFormats.add(BarcodeFormat.QR_CODE);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        // 设置继续的字符编码格式为UTF8
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");

        // 设置解析配置参数
        multiFormatReader.setHints(hints);

    }

    /**
     * 获取解码结果
     * 
     * @param bitmap
     * @return
     */
    public Result getRawResult(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        try {
            return multiFormatReader
                .decodeWithState(new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(bitmap))));
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
