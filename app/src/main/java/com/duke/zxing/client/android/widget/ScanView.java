package com.duke.zxing.client.android.widget;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.duke.zxing.client.android.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

/**
 * Created by Duke on 2016/10/21.
 */

public class ScanView extends RelativeLayout implements SurfaceHolder.Callback {

    public static final String KEY_BITMAP = "BITMAP";

    private static final String TAG = ScanView.class.getSimpleName();

    private ScanManager mScanManager;

    /**
     * 处理结果
     */
    private Result savedResultToShow;

    /**
     * 是否有Surface
     */
    private boolean hasSurface;

    /**
     * 支持的格式
     */
    private Collection<BarcodeFormat> decodeFormats;

    /**
     * 解码参数
     */
    private Map<DecodeHintType, ?> decodeHints;

    /**
     * 字符集
     */
    private String characterSet;

    /*  */
    /**
     * 蜂鸣管理
     *//*
       private BeepManager beepManager;*/

    private View mRootView;

    private Context mContext;
    private OnScanCallback mOnScanCallback;

    public ScanView(Context context) {
        super(context);
        System.out.println("init1");
        initView(context);
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        System.out.println("init2");
        initView(context);
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        System.out.println("init3");
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        System.out.println("init4");
        initView(context);
    }

    private void initView(Context context) {
        System.out.println("init");
        mContext = context.getApplicationContext();
        hasSurface = false;
        mScanManager = new ScanManager(mContext);
        mScanManager.init();

        mRootView = View.inflate(mContext, R.layout.layout_scan_view, this);
        System.out.println("init done");
        // beepManager = new BeepManager(mContext);
    }

    public void setOnScanCallback(OnScanCallback onScanCallback) {
        mScanManager.setOnScanCallback(onScanCallback);
        this.mOnScanCallback = onScanCallback;
    }

    public void onResume() {
        /*  cameraManager = new CameraManager(mContext);*/
        mScanManager.initCameraManager();
        mScanManager.setViewFinderView((ViewfinderView) mRootView.findViewById(R.id.viewfinder_view));

        mScanManager.setHandler(null);
        mScanManager.setLastResult(null);

        /*if (prefs.getBoolean(PreferencesActivity.KEY_DISABLE_AUTO_ORIENTATION, true)) {
          setRequestedOrientation(getCurrentOrientation());
        } else {
          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }*/

        resetStatusView();

        // beepManager.updatePrefs();
        mScanManager.startAmbientLightManager();

        decodeFormats = null;
        characterSet = null;

        SurfaceView surfaceView = (SurfaceView) mRootView.findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    public void onPause() {
        if (mScanManager.getHandler() != null) {
            mScanManager.getHandler().quitSynchronously();
            mScanManager.setHandler(null);
        }
        mScanManager.getAmbientLightManager().stop();
        // beepManager.close();
        mScanManager.getCameraManager().closeDriver();
        // historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (mScanManager.getCameraManager().isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mScanManager.getCameraManager().openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (mScanManager.getHandler() == null) {
                // handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
                mScanManager.setHandler(new ScanViewHandler(decodeFormats, decodeHints, characterSet, mScanManager));
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (mScanManager.getHandler() == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(mScanManager.getHandler(), R.id.decode_succeeded, savedResultToShow);
                mScanManager.getHandler().sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    private void resetStatusView() {
        mScanManager.showViewFinderView();
        mScanManager.setLastResult(null);
    }

    public void drawViewfinder() {
        mScanManager.drawViewFinder();
    }

    private void displayFrameworkBugMessageAndExit() {
        /*    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.app_name));
        builder.setMessage(mContext.getString(R.string.msg_camera_framework_bug));
        *//*     builder.setPositiveButton(R.string.button_ok, new FinishListener(mContext));
          builder.setOnCancelListener(new FinishListener(mContext));*//*
                                                                      builder.show();*/
        if (mOnScanCallback != null) {
            mOnScanCallback.onCameraOpenFailure();
        }
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (mScanManager.getHandler() != null) {
            mScanManager.getHandler().sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mScanManager.getLastResult() != null) {
                    restartPreviewAfterDelay(0L);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mScanManager.getCameraManager().setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                mScanManager.getCameraManager().setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }
}
