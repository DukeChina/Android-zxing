package com.duke.zxing.client.android;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.duke.zxing.client.android.widget.OnScanCallback;
import com.duke.zxing.client.android.widget.ScanView;
import com.google.zxing.Result;

/**
 * Created by Duke on 2016/10/21.
 */

public class ScanActivity extends Activity {

    ScanView mScanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mScanView = (ScanView) findViewById(R.id.scan_view);
        mScanView.setOnScanCallback(new OnScanCallback() {
            @Override
            public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {
                handleDecodeInternally(obj, barcode);
            }

            @Override
            public void onCameraOpenFailure() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                builder.setTitle(ScanActivity.this.getString(R.string.app_name));
                builder.setMessage(ScanActivity.this.getString(R.string.msg_camera_framework_bug));
                builder.setPositiveButton(R.string.button_ok, new FinishListener(ScanActivity.this));
                builder.setOnCancelListener(new FinishListener(ScanActivity.this));
                builder.show();
            }
        });
    }

    private void handleDecodeInternally(Result rawResult, Bitmap barcode) {

        Intent intent = new Intent();
        intent.putExtra(ShowActivity.KEY_QRSTRING, rawResult.toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        barcode.compress(Bitmap.CompressFormat.PNG, 100, baos);
        intent.putExtra(ShowActivity.KEY_QRBITMAP, baos.toByteArray());
        intent.setClass(this, ShowActivity.class);
        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mScanView == null) {
            mScanView = (ScanView) findViewById(R.id.scan_view);
            if (mScanView == null) {
                return;
            }
            mScanView.setOnScanCallback(new OnScanCallback() {
                @Override
                public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {
                    handleDecodeInternally(obj, barcode);
                }

                @Override
                public void onCameraOpenFailure() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                    builder.setTitle(ScanActivity.this.getString(R.string.app_name));
                    builder.setMessage(ScanActivity.this.getString(R.string.msg_camera_framework_bug));
                    builder.setPositiveButton(R.string.button_ok, new FinishListener(ScanActivity.this));
                    builder.setOnCancelListener(new FinishListener(ScanActivity.this));
                    builder.show();
                }
            });
        }
        mScanView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScanView != null) {
            mScanView.onPause();
        }
    }
}
