package com.dm.material.dashboard.candybar.applications;

import android.app.Application;
import android.content.Intent;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarCrashReport;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

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

public class CandyBarApplication extends Application {

    private Thread.UncaughtExceptionHandler mHandler;

    public void initApplication() {
        super.onCreate();
        if (!ImageLoader.getInstance().isInited())
            ImageLoader.getInstance().init(ImageConfig.getImageLoaderConfiguration(this));

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Font-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        //Enable or disable logging
        LogUtil.setLoggingEnabled(true);

        mHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    }

    private void handleUncaughtException(Thread thread, Throwable throwable) {
        try {
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String dateTime = dateFormat.format(new Date());
            sb.append("Crash Time : ").append(dateTime).append("\n");
            sb.append("Class Name : ").append(throwable.getClass().getName()).append("\n");
            sb.append("Caused By : ").append(throwable.toString()).append("\n");

            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("\n");
                sb.append(element.toString());
            }

            Preferences.getPreferences(this).setLatestCrashLog(sb.toString());

            Intent intent = new Intent(this, CandyBarCrashReport.class);
            intent.putExtra(CandyBarCrashReport.EXTRA_STACKTRACE, sb.toString());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        } catch (Exception e) {
            if (mHandler != null) {
                mHandler.uncaughtException(thread, throwable);
                return;
            }
        }
        System.exit(1);
    }

}
