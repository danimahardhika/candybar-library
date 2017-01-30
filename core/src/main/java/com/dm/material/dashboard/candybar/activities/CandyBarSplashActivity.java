package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.utils.Tag;

import java.util.Collections;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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

public class CandyBarSplashActivity extends AppCompatActivity {

    private Class<?> mMainActivity;
    private AsyncTask<Void, Request, Boolean> mPrepareIconRequest;

    public void initSplashActivity(Bundle savedInstanceState, Class<?> mainActivity) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mMainActivity = mainActivity;

        int titleColor = ColorHelper.getTitleTextColor(ContextCompat
                .getColor(this, R.color.splashColor));
        TextView splashTitle = (TextView) findViewById(R.id.splash_title);
        splashTitle.setTextColor(ColorHelper.setColorAlpha(titleColor, 0.6f ));

        prepareIconRequest();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        if (mPrepareIconRequest != null) mPrepareIconRequest.cancel(true);
        super.onDestroy();
    }

    private void prepareIconRequest() {
        mPrepareIconRequest = new AsyncTask<Void, Request, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        PackageManager packageManager = getPackageManager();

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        CandyBarMainActivity.sInstalledApps = packageManager.queryIntentActivities(
                                intent, PackageManager.GET_RESOLVED_FILTER);

                        try {
                            Collections.sort(CandyBarMainActivity.sInstalledApps,
                                    new ResolveInfo.DisplayNameComparator(getPackageManager()));
                        } catch (Exception ignored) {}
                        return true;
                    } catch (Exception e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                mPrepareIconRequest = null;
                startActivity(new Intent(CandyBarSplashActivity.this, mMainActivity));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }.execute();
    }

}

