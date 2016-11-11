package com.dm.material.dashboard.candybar.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.listeners.LicenseListener;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
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

public class LicenseHelper implements LicenseCheckerCallback {

    private Context mContext;
    private MaterialDialog mDialog;

    public static LicenseHelper getLicenseChecker(@NonNull Context context) {
        return new LicenseHelper(context);
    }

    private LicenseHelper(Context context) {
        mContext = context;
    }

    public void checkLicense(String licenseKey, byte[] salt) {
        if (isReadyToCheckLicense(salt)) {
            @SuppressLint("HardwareIds")
            String deviceId = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);

            LicenseChecker checker = new LicenseChecker(mContext,
                    new ServerManagedPolicy(mContext,
                            new AESObfuscator(salt, mContext.getPackageName(), deviceId)),
                    licenseKey);
            checker.checkAccess(this);
            getDialog().show();
        } else {
            Log.d(Tag.LOG_TAG, "Unable to check license, random bytes is wrong!");
        }
    }

    private boolean isReadyToCheckLicense(byte[] salt) {
        if (salt != null) {
            if (salt.length == 20) return true;
        }
        return false;
    }

    @Override
    public void allow(int reason) {
        getDialog().dismiss();
        mDialog = null;
        showLicenseDialog(reason);
    }

    @Override
    public void dontAllow(int reason) {
        getDialog().dismiss();
        mDialog = null;
        if (reason == Policy.RETRY) {
            showRetryDialog();
        } else if (reason == Policy.NOT_LICENSED) {
            showLicenseDialog(reason);
        }
    }

    @Override
    public void applicationError(int errorCode) {
        getDialog().dismiss();
        mDialog = null;
        if (errorCode == ERROR_NOT_MARKET_MANAGED) {
            showLicenseDialog(Policy.NOT_LICENSED);
            return;
        }

        Toast.makeText(mContext, R.string.license_check_error, Toast.LENGTH_LONG).show();
        ((AppCompatActivity) mContext).finish();
    }

    private MaterialDialog getDialog() {
        if (mDialog == null) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
            builder.content(R.string.license_checking)
                    .progress(true, 0);

            mDialog = builder.build();
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
        }
        return mDialog;
    }

    private void showLicenseDialog(int reason) {
        int message = reason == Policy.LICENSED ?
                R.string.license_check_success : R.string.license_check_failed;
        new MaterialDialog.Builder(mContext)
                .title(R.string.license_check)
                .content(message)
                .positiveText(R.string.close)
                .onPositive((dialog, which) -> {
                    try {
                        LicenseListener listener = (LicenseListener) mContext;
                        listener.OnLicenseChecked(reason);
                    } catch (Exception ignored) {}
                })
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();
    }

    private void showRetryDialog() {
        new MaterialDialog.Builder(mContext)
                .title(R.string.license_check)
                .content(R.string.license_check_retry)
                .positiveText(R.string.close)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .onPositive((dialog, which) -> ((AppCompatActivity) mContext).finish())
                .show();
    }

}
