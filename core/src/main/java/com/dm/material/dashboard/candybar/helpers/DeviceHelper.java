package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import com.dm.material.dashboard.candybar.BuildConfig;
import com.dm.material.dashboard.candybar.R;

import android.util.DisplayMetrics;

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

public class DeviceHelper {

    @NonNull
    public static String getDeviceInfo(@NonNull Context context) {
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        StringBuilder sb = new StringBuilder();
        final int height = displaymetrics.heightPixels;
        final int width = displaymetrics.widthPixels;

        String appVersion = "";
        try {
            appVersion = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignored) {}

        sb.append("Manufacturer : ").append(Build.MANUFACTURER)
                .append("\nModel : ").append(Build.MODEL)
                .append("\nProduct : ").append(Build.PRODUCT)
                .append("\nScreen Resolution : ")
                .append(width).append(" x ").append(height).append(" pixels")
                .append("\nAndroid Version : ").append(Build.VERSION.RELEASE)
                .append("\nApp Version : ").append(appVersion)
                .append("\nCandyBar Version : ").append(BuildConfig.VERSION_NAME)
                .append("\n");
        return sb.toString();
    }

    @NonNull
    public static String getDeviceInfoForCrashReport(@NonNull Context context) {
        return "Icon Pack Name : " +context.getResources().getString(R.string.app_name)
                + "\n"+ getDeviceInfo(context);
    }
}
