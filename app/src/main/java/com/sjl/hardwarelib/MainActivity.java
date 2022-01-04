package com.sjl.hardwarelib;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.renny.zxing.Activity.CaptureActivity;
import com.sinpo.xnfc.NFCardActivity;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void btnNfcReadCard(View view) {
        startActivity(new Intent(this, NFCardActivity.class));
    }

    public void btnZxingScan(View view) {
        startActivity(new Intent(this, CaptureActivity.class));
    }
}
