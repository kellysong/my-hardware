package com.sjl.hardwarelib;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.renny.zxing.Activity.CaptureActivity;
import com.renny.zxing.utils.QRCodeFactory;
import com.sinpo.xnfc.NFCardActivity;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    ImageView mQrCode,mQrCode2,mBarCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mQrCode = findViewById(R.id.iv_qr_code);
        mQrCode2 = findViewById(R.id.iv_qr_code2);
        mBarCode = findViewById(R.id.iv_bar_code);
        requestPermissions(100);

    }

    public void btnNfcReadCard(View view) {
        startActivity(new Intent(this, NFCardActivity.class));
    }

    public void btnZxingScan(View view) {
        startActivity(new Intent(this, CaptureActivity.class));
    }


    // 请求权限
    public void requestPermissions(int requestCode) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                ArrayList<String> requestPerssionArr = new ArrayList<>();
                int hasCamrea = checkSelfPermission(Manifest.permission.CAMERA);
                if (hasCamrea != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.CAMERA);
                }

                int hasSdcardRead = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                if (hasSdcardRead != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                int hasSdcardWrite = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasSdcardWrite != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                // 是否应该显示权限请求
                if (requestPerssionArr.size() >= 1) {
                    String[] requestArray = new String[requestPerssionArr.size()];
                    for (int i = 0; i < requestArray.length; i++) {
                        requestArray[i] = requestPerssionArr.get(i);
                    }
                    requestPermissions(requestArray, requestCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void btnCodeCreate(View view) {
        int w = getResources().getDimensionPixelSize(R.dimen.dp_200);
        int h = getResources().getDimensionPixelSize(R.dimen.dp_80);

        try {
            Bitmap qrCode = QRCodeFactory.createQRCode("http://www.baidu.com", w);
            mQrCode.setImageBitmap(qrCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_qr_logo);
            Bitmap qrCode = QRCodeFactory.createQRImage("http://www.baidu.com", w,bitmap);
            mQrCode2.setImageBitmap(qrCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Bitmap qrCode = QRCodeFactory.createBarcode("FF123563233", w,h,true);
            mBarCode.setImageBitmap(qrCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean flag = false;
        for (int i = 0; i < permissions.length; i++) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[i]) {
                flag = true;
            }
        }
    }


}
