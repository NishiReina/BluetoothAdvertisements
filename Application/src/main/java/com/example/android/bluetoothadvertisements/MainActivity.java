/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.bluetoothadvertisements;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Setup display fragments and ensure the device supports Bluetooth.
 */
public class MainActivity extends FragmentActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_PERMISSIONS = 2;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.activity_main_title);

        if (savedInstanceState == null) {

            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                    .getAdapter();

            // このデバイスがBluetoothに対応しているか？
            if (mBluetoothAdapter != null) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    }, REQUEST_PERMISSIONS);
                }

                // Bluetoothはオンになっているか？
                if (mBluetoothAdapter.isEnabled()) {

                    // このデバイスが、Bluetoothアドバタイジングに対応しているか？
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
//                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
////                            requestPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
//                            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADVERTISE
//                            }, REQUEST_PERMISSIONS);
                        // 全てがサポートされ有効になっているので、フラグメントを読み込む
                        setupFragments();
//                    }

                    } else {

                        // Bluetooth アドバタイズメントに対応していない
                        showErrorText(R.string.bt_ads_not_supported);
                    }
                } else {

                    // Bluetoothをオンにすることを要求する
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
                }
            } else {

                // Bluetoothには対応していません。
                showErrorText(R.string.bt_not_supported);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                finish();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:

                if (resultCode == RESULT_OK) {

                    // Bluetoothが使用不可になったが、Bluetoothアドバタイズに対応しているか？
                    // this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                        // 全てがサポートされ有効になっているので、フラグメントを読み込む
                        setupFragments();

                    } else {

                        // Bluetooth Advertisementsはサポートされていません
                        showErrorText(R.string.bt_ads_not_supported);
                    }
                } else {

                    // ユーザーがBluetoothの有効化を拒否した場合は、アプリを終了してください。
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupFragments() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        ScannerFragment scannerFragment = new ScannerFragment();
        // フラグメントはシステムサービスに直接アクセスできないので、BluetoothAdapterを渡します。
        scannerFragment.setBluetoothAdapter(mBluetoothAdapter);
        transaction.replace(R.id.scanner_fragment_container, scannerFragment);

        AdvertiserFragment advertiserFragment = new AdvertiserFragment();
        transaction.replace(R.id.advertiser_fragment_container, advertiserFragment);

        transaction.commit();
    }

    private void showErrorText(int messageId) {

        TextView view = (TextView) findViewById(R.id.error_textview);
        view.setText(getString(messageId));
    }
}