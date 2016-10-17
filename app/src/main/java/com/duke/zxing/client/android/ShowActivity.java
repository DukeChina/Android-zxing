package com.duke.zxing.client.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowActivity extends Activity {

    public final static String KEY_QRBITMAP = "QRCode";

    public final static String KEY_QRSTRING = "QRString";

    private ImageView mQRCode;

    private TextView mCodeStr;

    private String mString;

    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        if (getIntent() == null || getIntent().getExtras() == null) {
            finish();
            return;
        }

        mString = getIntent().getExtras().getString(KEY_QRSTRING);

        byte[] arr  = getIntent().getExtras().getByteArray(KEY_QRBITMAP);

        mBitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length, null);

        mQRCode = (ImageView) findViewById(R.id.QRCode);

        mCodeStr = (TextView) findViewById(R.id.codeStr);
        if (mString != null) {
            mCodeStr.setText(mString);
        }

        if (mBitmap != null) {
            mQRCode.setImageBitmap(mBitmap);
        }
    }
}
