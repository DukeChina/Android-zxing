/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duke.zxing.client.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.duke.zxing.client.android.utils.CommonUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

/**
 * Barcode Scanner can share data like contacts and bookmarks by displaying a QR Code on screen,
 * such that another user can scan the barcode with their phone.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ShareActivity extends Activity {

    private static final String TAG = ShareActivity.class.getSimpleName();

    private final View.OnKeyListener textListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                String text = ((TextView) view).getText().toString();
                if (text != null && !text.isEmpty()) {
                    launchSearch(text);
                }
                return true;
            }
            return false;
        }
    };

    private void launchSearch(String text) {
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        int width = displaySize.x;
        int height = displaySize.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 7 / 8;
        AlertDialog.Builder s = new AlertDialog.Builder(this);
        ImageView imageView = new ImageView(this);
        try {
            imageView.setImageBitmap(CommonUtil.encodeStr2Bitmap(text,smallerDimension));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        s.setView(imageView);
        s.setMessage(text);
        s.create().show();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.share);

        findViewById(R.id.share_text_view).setOnKeyListener(textListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
