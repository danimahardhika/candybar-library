package com.dm.material.dashboard.candybar.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.DeviceHelper;
import com.dm.material.dashboard.candybar.helpers.FileHelper;
import com.dm.material.dashboard.candybar.helpers.PermissionHelper;
import com.dm.material.dashboard.candybar.helpers.ReportBugsHelper;

import java.io.File;

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

public class CandyBarCrashReport extends AppCompatActivity {

    public static final String EXTRA_STACKTRACE = "stacktrace";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                finish();
                return;
            }

            String stackTrace = bundle.getString(EXTRA_STACKTRACE);
            String deviceInfo = DeviceHelper.getDeviceInfoForCrashReport(this);
            String crashLog = ReportBugsHelper.buildCrashLog(this,
                    FileHelper.getCacheDirectory(this), stackTrace);
            boolean granted = PermissionHelper.isPermissionStorageGranted(this);

            String message = getResources().getString(R.string.crash_report_message) +" "+
                    getResources().getString(R.string.app_name) +" "+
                    getResources().getString(R.string.crash_report_message_1);
            new MaterialDialog.Builder(this)
                    .title(R.string.crash_report)
                    .content(message)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .positiveText(R.string.crash_report_send)
                    .negativeText(R.string.close)
                    .onPositive((dialog, which) -> {
                        final Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("message/rfc822");
                        intent.putExtra(Intent.EXTRA_EMAIL,
                                new String[]{getResources().getString(R.string.dev_email)});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "CandyBar: Crash Report");

                        if (crashLog != null && granted) {
                            Uri uri = FileHelper.getUriFromFile(this, getPackageName(), new File(crashLog));
                            if (uri == null) uri = Uri.fromFile(new File(crashLog));
                            intent.putExtra(Intent.EXTRA_TEXT, deviceInfo +"\n");
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                        } else {
                            intent.putExtra(Intent.EXTRA_TEXT, deviceInfo + stackTrace);
                        }

                        startActivity(Intent.createChooser(intent,
                                getResources().getString(R.string.email_client)));
                        dialog.dismiss();
                    })
                    .dismissListener(dialogInterface -> finish())
                    .show();
        } catch (Exception e) {
            finish();
        }
    }

}
