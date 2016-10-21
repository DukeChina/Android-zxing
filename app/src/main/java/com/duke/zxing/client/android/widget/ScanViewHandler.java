package com.duke.zxing.client.android.widget;

import java.util.Collection;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.duke.zxing.client.android.R;
import com.duke.zxing.client.android.decode.BitmapDecoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

/**
 * Created by Duke on 2016/10/21.
 */

public class ScanViewHandler extends Handler {

    private static final String TAG = ScanViewHandler.class.getSimpleName();
    private final ScanViewThread decodeThread;
    private final ScanManager scanManager;
    private ScanViewHandler.State state;

    public ScanViewHandler(Collection<BarcodeFormat> decodeFormats, Map<DecodeHintType, ?> baseHints,
        String characterSet, ScanManager scanManager) {
        decodeThread = new ScanViewThread(decodeFormats, baseHints, characterSet, scanManager);
        decodeThread.start();
        state = ScanViewHandler.State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.scanManager = scanManager;
        scanManager.getCameraManager().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.restart_preview:
                restartPreviewAndDecode();
                break;
            case R.id.decode_succeeded:
                state = ScanViewHandler.State.SUCCESS;
                Bundle bundle = message.getData();
                Bitmap barcode = null;
                float scaleFactor = 1.0f;
                if (bundle != null) {
                    byte[] compressedBitmap = bundle.getByteArray(ScanViewThread.BARCODE_BITMAP);
                    if (compressedBitmap != null) {
                        barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                        // Mutable copy:
                        barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    scaleFactor = bundle.getFloat(ScanViewThread.BARCODE_SCALED_FACTOR);
                }
                scanManager.handleDecode((Result) message.obj, barcode, scaleFactor);
                break;
            case R.id.decode_failed:
                // We're decoding as fast as possible, so when one decode fails, start another.
                state = ScanViewHandler.State.PREVIEW;
                scanManager.getCameraManager().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
                break;
            case R.id.launch_product_query:
                Bundle bundles = message.getData();
                if (bundles != null) {
                    byte[] compressedBitmap = bundles.getByteArray(ScanView.KEY_BITMAP);
                    if (compressedBitmap != null) {
                        barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);

                        Result rawResult = null;
                        rawResult = new BitmapDecoder().getRawResult(barcode);
                        Handler handler = scanManager.getHandler();
                        if (rawResult != null) {
                            if (handler != null) {
                                Message message1 = Message.obtain(handler, R.id.decode_succeeded, rawResult);
                                Bundle bundle1 = new Bundle();
                                bundle1.putByteArray(ScanViewThread.BARCODE_BITMAP, compressedBitmap);
                                bundle1.putFloat(ScanViewThread.BARCODE_SCALED_FACTOR, 1);
                                message1.setData(bundle1);
                                message1.sendToTarget();
                            }
                        } else {
                            if (handler != null) {
                                Message message1 = Message.obtain(handler, R.id.decode_failed);
                                message1.sendToTarget();
                            }
                        }
                    }
                }
                break;
        }
    }

    public void quitSynchronously() {
        state = ScanViewHandler.State.DONE;
        scanManager.getCameraManager().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    private void restartPreviewAndDecode() {
        if (state == ScanViewHandler.State.SUCCESS) {
            state = ScanViewHandler.State.PREVIEW;
            scanManager.getCameraManager().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
            scanManager.drawViewFinder();
        }
    }

    private enum State {
        PREVIEW, SUCCESS, DONE
    }
}
