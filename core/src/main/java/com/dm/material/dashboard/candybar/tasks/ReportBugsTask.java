package com.dm.material.dashboard.candybar.tasks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.DeviceHelper;
import com.dm.material.dashboard.candybar.helpers.ReportBugsHelper;
import com.dm.material.dashboard.candybar.helpers.RequestHelper;
import com.dm.material.dashboard.candybar.helpers.TypefaceHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static com.danimahardhika.android.helpers.core.FileHelper.getUriFromFile;

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

public class ReportBugsTask extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private String mDescription;
    private String mZipPath = null;
    private StringBuilder mStringBuilder;
    private MaterialDialog mDialog;

    private ReportBugsTask(Context context, String description) {
        mContext = context;
        mDescription = description;
    }

    public static AsyncTask start(@NonNull Context context, @NonNull String description) {
        return start(context, description, SERIAL_EXECUTOR);
    }

    public static AsyncTask start(@NonNull Context context, @NonNull String description, @NonNull Executor executor) {
        return new ReportBugsTask(context, description).executeOnExecutor(executor);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
        builder.typeface(
                TypefaceHelper.getMedium(mContext),
                TypefaceHelper.getRegular(mContext))
                .content(R.string.report_bugs_building)
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .canceledOnTouchOutside(false);

        mDialog = builder.build();
        mDialog.show();
        mStringBuilder = new StringBuilder();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                Thread.sleep(1);
                List<String> files = new ArrayList<>();

                mStringBuilder.append(DeviceHelper.getDeviceInfo(mContext))
                        .append("\n").append(mDescription).append("\n");

                File brokenAppFilter = ReportBugsHelper.buildBrokenAppFilter(mContext);
                if (brokenAppFilter != null) files.add(brokenAppFilter.toString());

                File brokenDrawables = ReportBugsHelper.buildBrokenDrawables(mContext);
                if (brokenDrawables != null) files.add(brokenDrawables.toString());

                File activityList = ReportBugsHelper.buildActivityList(mContext);
                if (activityList != null) files.add(activityList.toString());

                String stackTrace = Preferences.get(mContext).getLatestCrashLog();
                File crashLog = ReportBugsHelper.buildCrashLog(mContext, stackTrace);
                if (crashLog != null) files.add(crashLog.toString());

                mZipPath = FileHelper.createZip(files, new File(mContext.getCacheDir(),
                        RequestHelper.getGeneratedZipName(ReportBugsHelper.REPORT_BUGS)));
                return true;
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        mDialog.dismiss();
        if (aBoolean) {
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{mContext.getResources().getString(R.string.dev_email)});
            intent.putExtra(Intent.EXTRA_SUBJECT,
                    "Report Bugs " + (mContext.getString(
                            R.string.app_name)));
            intent.putExtra(Intent.EXTRA_TEXT, mStringBuilder.toString());

            if (mZipPath != null) {
                File zip = new File(mZipPath);
                if (zip.exists()) {
                    Uri uri = getUriFromFile(mContext, mContext.getPackageName(), zip);
                    if (uri == null) uri = Uri.fromFile(zip);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

            mContext.startActivity(Intent.createChooser(intent,
                    mContext.getResources().getString(R.string.email_client)));
        } else {
            Toast.makeText(mContext, R.string.report_bugs_failed,
                    Toast.LENGTH_LONG).show();
        }

        mZipPath = null;
    }
}
