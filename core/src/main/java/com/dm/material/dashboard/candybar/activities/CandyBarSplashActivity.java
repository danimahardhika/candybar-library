package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.items.WallpaperJSON;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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
    private AsyncTask<Void, Void, Boolean> mPrepareIconsList;
    private AsyncTask<Void, Void, Boolean> mCheckRszIo;
    private AsyncTask<Void, Void, Boolean> mPrepareCloudWallpapers;

    public void initSplashActivity(Bundle savedInstanceState, Class<?> mainActivity) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mMainActivity = mainActivity;

        int titleColor = ColorHelper.getTitleTextColor(ContextCompat
                .getColor(this, R.color.splashColor));
        TextView splashTitle = (TextView) findViewById(R.id.splash_title);
        splashTitle.setTextColor(ColorHelper.setColorAlpha(titleColor, 0.7f));

        prepareIconsList();
        checkRszIo();
        prepareCloudWallpapers(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        if (mPrepareCloudWallpapers != null) mPrepareCloudWallpapers.cancel(true);
        if (mCheckRszIo != null) mCheckRszIo.cancel(true);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mPrepareIconsList != null) mPrepareIconsList.cancel(true);
        super.onDestroy();
    }

    private void prepareIconsList() {
        mPrepareIconsList = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        CandyBarMainActivity.sSections = IconsHelper
                                .getIconsList(CandyBarSplashActivity.this);

                        int count = 0;
                        for (Icon section : CandyBarMainActivity.sSections) {
                            count += section.getIcons().size();
                        }
                        CandyBarMainActivity.sIconsCount = count;
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
                mPrepareIconsList = null;
                startActivity(new Intent(CandyBarSplashActivity.this, mMainActivity));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void checkRszIo() {
        mCheckRszIo = new AsyncTask<Void, Void, Boolean>() {

            final String rszio = "https://rsz.io/";

            @Override
            protected Boolean doInBackground(Void... voids) {
                while ((!isCancelled())) {
                    try {
                        Thread.sleep(1);
                        URL url = new URL(rszio);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setReadTimeout(6000);
                        connection.setConnectTimeout(6000);
                        int code = connection.getResponseCode();
                        return code == 200;
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
                CandyBarMainActivity.sRszIoAvailable = aBoolean;
                LogUtil.e("rsz.io availability: " +CandyBarMainActivity.sRszIoAvailable);
                mCheckRszIo = null;
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

                        Database database = new Database(context);
                        if (database.getWallpapersCount() > 0) return true;

                        URL url = new URL(wallpaperUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(15000);
                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream stream = connection.getInputStream();
                            WallpaperJSON wallpapersJSON = LoganSquare.parse(stream, WallpaperJSON.class);
                            if (database.getWallpapersCount() > 0) database.deleteWallpapers();
                            database.addWallpapers(wallpapersJSON);
                        }

                        List<Wallpaper> wallpapers = database.getWallpapers();
                        if (wallpapers.size() > 0) {
                            String uri = WallpaperHelper.getThumbnailUrl(context,
                                    wallpapers.get(0).getURL(),
                                    wallpapers.get(0).getThumbUrl());
                            ImageLoader.getInstance().loadImageSync(uri,
                                    ImageConfig.getThumbnailSize(context),
                                    ImageConfig.getDefaultImageOptions(true));
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

