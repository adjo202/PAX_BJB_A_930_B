package com.pax.pay.trans.receipt;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.pax.pay.app.FinancialApplication;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wangyq on 2017/8/28.
 */

public class ReceiptGeneratorBase {
    private static final String TAG = "ReceiptGeneratorSettle";

    /**
     * @param fileName
     * @return Bitmap, from the Asserts file.
     */
    protected Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = FinancialApplication.getAppContext().getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "", e);
        }
        return image;
    }
}
