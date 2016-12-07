package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.preferences.Preferences;

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

public class RequestHelper {

    public static void showAlreadyRequestedDialog(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.request_title)
                .content(R.string.request_requested)
                .positiveText(R.string.close)
                .show();
    }

    public static void showIconRequestLimitDialog(@NonNull Context context) {
        boolean reset = context.getResources().getBoolean(R.bool.reset_icon_request_limit);
        int limit = context.getResources().getInteger(R.integer.icon_request_limit);
        String message = context.getResources().getString(R.string.request_limit) +" "+ limit +" "+
                context.getResources().getString(R.string.request_limit_1) +" "+
                context.getResources().getString(R.string.request_limit_2) +" "+
                Preferences.getPreferences(context).getRegularRequestUsed() +" "+
                context.getResources().getString(R.string.request_limit_3);
        if (reset) message = message +"\n\n"+ context.getResources().getString(R.string.request_limit_reset);
        new MaterialDialog.Builder(context)
                .title(R.string.request_title)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestLimitDialog(@NonNull Context context, int selected) {
        String message = context.getResources().getString(R.string.premium_request_limit) +" "+
                Preferences.getPreferences(context).getPremiumRequestCount() +" "+
                context.getResources().getString(R.string.premium_request_limit_1) +" "+
                selected +" "+ context.getResources().getString(R.string.premium_request_limit_2) +
                "\n\n"+ context.getResources().getString(R.string.premium_request_limit_3);
        new MaterialDialog.Builder(context)
                .title(R.string.premium_request)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestStillAvailable(@NonNull Context context) {
        String message = context.getResources().getString(R.string.premium_request_already_purchased)
                +" "+ Preferences.getPreferences(context).getPremiumRequestCount() +" "+
                context.getResources().getString(R.string.premium_request_already_purchased_1);
        new MaterialDialog.Builder(context)
                .title(R.string.premium_request)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static boolean isReadyToSendPremiumRequest(@NonNull Context context) {
        boolean isReady = Preferences.getPreferences(context).isConnectedToNetwork();
        boolean granted = PermissionHelper.isPermissionStorageGranted(context);
        if (!isReady) {
            new MaterialDialog.Builder(context)
                    .title(R.string.premium_request)
                    .content(R.string.premium_request_no_internet)
                    .positiveText(R.string.close)
                    .show();
        } else if (!granted) {
            PermissionHelper.showRequestPermissionStorageDenied(context);
        }
        return isReady && granted;
    }

    public static void showPremiumRequestConsumeFailed(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.premium_request)
                .content(R.string.premium_request_consume_failed)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestExist(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.premium_request)
                .content(R.string.premium_request_exist)
                .positiveText(R.string.close)
                .show();
    }

    public static void checkPiracyApp(@NonNull Context context) {

        //Lucky Patcher and Freedom package name
        String[] strings = new String[] {
                "com.chelpus.lackypatch",
                "com.dimonvideo.luckypatcher",
                "com.forpda.lp",
                "com.android.protips",
                "com.android.vending.billing.InAppBillingService.LUCK",
                "com.android.vending.billing.InAppBillingService.LOCK",
                "cc.madkite.freedom",
                "com.android.vending.billing.InAppBillingService.LACK"
        };

        for (String string : strings) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                        string, PackageManager.GET_ACTIVITIES);
                if (packageInfo != null) {
                    Preferences.getPreferences(context).setPremiumRequestEnabled(false);
                    return;
                }
            } catch (Exception ignored) {}
        }

        Preferences.getPreferences(context).setPremiumRequestEnabled(
                context.getResources().getBoolean(R.bool.enable_premium_request));
    }

}
