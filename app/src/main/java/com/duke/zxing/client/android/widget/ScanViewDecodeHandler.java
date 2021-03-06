package com.duke.zxing.client.android.widget;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

/**
 * Created by Duke on 2016/10/21.
 */

public class ScanViewDecodeHandler extends Handler {

    private static final String TAG = ScanViewDecodeHandler.class.getSimpleName();
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;
    private ScanManager mScanManager;

    ScanViewDecodeHandler(Map<DecodeHintType, Object> hints, ScanManager scanManager) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.mScanManager = scanManager;
    }

    private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(ScanViewThread.BARCODE_BITMAP, out.toByteArray());
        bundle.putFloat(ScanViewThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case com.duke.zxing.client.android.R.id.decode:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case com.duke.zxing.client.android.R.id.quit:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width;
        width = height;
        height = tmp;
        data = rotatedData;
        long start = System.currentTimeMillis();
        Result rawResult = null;
        PlanarYUVLuminanceSource source = mScanManager.getCameraManager().buildLuminanceSource(data, width, height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        Handler handler = mScanManager.getHandler();
        if (rawResult != null) {
            // Don't log the barcode contents for security.
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode in " + (end - start) + " ms");
            if (handler != null) {
                Message message =
                    Message.obtain(handler, com.duke.zxing.client.android.R.id.decode_succeeded, rawResult);
                Bundle bundle = new Bundle();
                bundleThumbnail(source, bundle);
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (handler != null) {
                Message message = Message.obtain(handler, com.duke.zxing.client.android.R.id.decode_failed);
                message.sendToTarget();
            }
        }
    }
}
