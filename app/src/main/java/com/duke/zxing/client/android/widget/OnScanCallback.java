package com.duke.zxing.client.android.widget;

import android.graphics.Bitmap;

import com.google.zxing.Result;

/**
 * Created by Duke on 2016/10/21.
 */

public interface OnScanCallback {

    void handleDecode(Result obj, Bitmap barcode, float scaleFactor);

    void onCameraOpenFailure();
}
