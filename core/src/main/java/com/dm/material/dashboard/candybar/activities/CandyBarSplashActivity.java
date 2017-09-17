package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.helpers.JsonHelper;
import com.dm.material.dashboard.candybar.helpers.LocaleHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

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
    private AsyncTask<Void, Void, Boolean> mPrepareApp;
    private AsyncTask<Void, Void, Boolean> mPrepareCloudWallpapers;

    public void initSplashActivity(Bundle savedInstanceState, Class<?> mainActivity) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mMainActivity = mainActivity;

        int color = ColorHelper.getBodyTextColor(ColorHelper.get(this, R.color.splashColor));
        TextView splashTitle = (TextView) findViewById(R.id.splash_title);
        splashTitle.setTextColor(color);

        prepareApp();
        prepareCloudWallpapers(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        if (mPrepareCloudWallpapers != null) {
            mPrepareCloudWallpapers.cancel(true);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mPrepareApp != null) {
            mPrepareApp.cancel(true);
        }
        super.onDestroy();
    }

    private void prepareApp() {
        mPrepareApp = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(400);
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
                mPrepareApp = null;
                Intent intent = new Intent(CandyBarSplashActivity.this, mMainActivity);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void prepareCloudWallpapers(@NonNull Context context) {
        final String wallpaperUrl = getResources().getString(R.string.wallpaper_json);

        mPrepareCloudWallpapers = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (WallpaperHelper.getWallpaperType(context) != WallpaperHelper.CLOUD_WALLPAPERS)
                            return true;

                        if (Database.get(context.getApplicationContext()).getWallpapersCount() > 0)
                            return true;

                        URL url = new URL(wallpaperUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(15000);
                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream stream = connection.getInputStream();
                            List list = JsonHelper.parseList(stream);
                            if (list == null) {
                                LogUtil.e("Json error, no array with name: "
                                        + CandyBarApplication.getConfiguration().getWallpaperJsonStructure().getArrayName());
                                return false;
                            }

                            if (Database.get(context.getApplicationContext()).getWallpapersCount() > 0) {
                                Database.get(context.getApplicationContext()).deleteWallpapers();
                            }

                            Database.get(context.getApplicationContext()).addWallpapers(list);

                            if (list.size() > 0 && list.get(0) instanceof Map) {
                                Map map = (Map) list.get(0);
                                String thumbUrl = JsonHelper.getThumbUrl(map);
                                ImageLoader.getInstance().loadImageSync(thumbUrl,
                                        ImageConfig.getThumbnailSize(),
                                        ImageConfig.getDefaultImageOptions(true));
                            }
                        }
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
                mPrepareCloudWallpapers = null;
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
}

