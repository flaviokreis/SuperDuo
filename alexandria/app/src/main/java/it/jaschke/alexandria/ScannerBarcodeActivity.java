package it.jaschke.alexandria;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerBarcodeActivity extends ActionBarActivity implements
        ZXingScannerView.ResultHandler{

    private static final String TAG = ScannerBarcodeActivity.class.getSimpleName();

    public static final String BARCODE_RESULT = "barcode_result";

    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);

        //Scan only barcode EAN_13
        List<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.EAN_13);
        mScannerView.setFormats(barcodeFormats);

        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        if(rawResult != null){
            Log.v(TAG, rawResult.getText());
            Log.v(TAG, rawResult.getBarcodeFormat().toString());

            Intent data = new Intent();
            data.putExtra(BARCODE_RESULT, rawResult.getText());

            setResult(RESULT_OK, data);

            finish();
        }
    }
}
