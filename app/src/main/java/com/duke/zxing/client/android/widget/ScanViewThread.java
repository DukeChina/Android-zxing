package com.duke.zxing.client.android.widget;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.duke.zxing.client.android.decode.DecodeFormatManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;

/**
 * Created by Duke on 2016/10/21.
 */

public class ScanViewThread extends Thread {

    public static final String BARCODE_BITMAP = "barcode_bitmap";
    public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";
    private final Map<DecodeHintType, Object> hints;
    private final CountDownLatch handlerInitLatch;
    private Handler handler;
    private ScanManager mScanManager;

    public ScanViewThread(Collection<BarcodeFormat> decodeFormats, Map<DecodeHintType, ?> baseHints,
        String characterSet, ScanManager scanManager) {

        mScanManager = scanManager;
        handlerInitLatch = new CountDownLatch(1);

        hints = new EnumMap<>(DecodeHintType.class);
        if (baseHints != null) {
            hints.putAll(baseHints);
        }

        // The prefs can't change while the thread is running, so pick them up once here.
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }
        Log.i("DecodeThread", "Hints: " + hints);
    }

    public Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new ScanViewDecodeHandler(hints, mScanManager);
        handlerInitLatch.countDown();
        Looper.loop();
    }
}
