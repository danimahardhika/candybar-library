package com.dm.material.dashboard.candybar.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
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

public class PermissionHelper {

    public static final int PERMISSION_STORAGE = 1;
    public static final int PERMISSION_STORAGE_SETTINGS = 2;

    public static boolean isPermissionStorageGranted(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int read = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.READ_EXTERNAL_STORAGE);
            int write = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED &&
                    write == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestStoragePermission(@NonNull Context context, int code) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions((AppCompatActivity) context,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, code);
        }
    }

    public static void showPermissionStorageDenied(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.permission_storage)
                .content(R.string.permission_storage_denied)
                .positiveText(R.string.close)
                .show();
    }

    public static void showRequestPermissionStorageDenied(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.permission_storage)
                .content(R.string.request_permission_storage_denied)
                .positiveText(R.string.close)
                .show();
    }

}
