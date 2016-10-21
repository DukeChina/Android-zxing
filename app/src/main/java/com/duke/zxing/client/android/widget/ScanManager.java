package com.duke.zxing.client.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.duke.zxing.client.android.R;
import com.duke.zxing.client.android.camera.CameraManager;
import com.duke.zxing.client.android.utils.AmbientLightManager;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

/**
 * Created by Duke on 2016/10/21.
 */

public class ScanManager {

    private Context mContext;

    /**
     * 震动管理
     */
    private AmbientLightManager ambientLightManager;
    /**
     * 相机管理
     */
    private CameraManager cameraManager;

    private ViewfinderView mViewfinderView;

    /**
     * 处理事件的handler
     */
    private ScanViewHandler handler;
    private Result lastResult;

    public void setOnScanCallback(OnScanCallback onScanCallback) {
        mOnScanCallback = onScanCallback;
    }

    private OnScanCallback mOnScanCallback;

    public ScanManager(Context context) {
        this.mContext = context;
    }

    public void init() {
        initAmbientLightManager();
    }

    private void initAmbientLightManager() {
        if (ambientLightManager == null) {
            ambientLightManager = new AmbientLightManager(mContext);
        }
    }

    public void initCameraManager() {
        cameraManager = new CameraManager(mContext);
    }

    public void startAmbientLightManager() {
        initAmbientLightManager();
        if (cameraManager == null) {
            initCameraManager();
        }
        ambientLightManager.start(cameraManager);
    }

    public AmbientLightManager getAmbientLightManager() {
        initAmbientLightManager();
        return ambientLightManager;
    }

    public CameraManager getCameraManager() {
        if (cameraManager == null) {
            initCameraManager();
        }
        return cameraManager;
    }

    public ScanViewHandler getHandler() {
        return handler;
    }

    public void setHandler(ScanViewHandler handler) {
        this.handler = handler;
    }

    public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {

        lastResult = obj;

        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            // Then not from history, so beep/vibrate and we have an image to draw on
            drawResultPoints(barcode, scaleFactor, obj);
        }

        /*  switch (source) {
            case NATIVE_APP_INTENT:
                handleDecodeExternally(rawResult, barcode);
                break;
            case NONE:*/
        // handleDecodeInternally(rawResult, barcode);
        /*   break;*/
        /* }*/

        if (mOnScanCallback != null) {
            mOnScanCallback.handleDecode(obj, barcode, scaleFactor);
        }
    }

    private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(mContext.getResources().getColor(R.color.result_points));

            paint.setStrokeWidth(10.0f);
            for (ResultPoint point : points) {
                if (point != null) {
                    canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
                }
            }

        }
    }

    public void setViewFinderView(ViewfinderView viewfinderView) {
        this.mViewfinderView = viewfinderView;
        viewfinderView.setCameraManager(cameraManager);
    }

    public void showViewFinderView() {
        if (this.mViewfinderView != null) {
            mViewfinderView.setVisibility(View.VISIBLE);
        }
    }

    public void drawViewFinder() {
        if (mViewfinderView != null) {
            mViewfinderView.drawViewfinder();
        }
    }

    public Result getLastResult() {
        return lastResult;
    }

    public void setLastResult(Result lastResult) {
        this.lastResult = lastResult;
    }
}
