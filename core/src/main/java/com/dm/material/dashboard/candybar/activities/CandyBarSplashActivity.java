package com.dm.material.dashboard.candybar.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.callbacks.SplashScreenCallback;
import com.dm.material.dashboard.candybar.activities.configurations.SplashScreenConfiguration;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.helpers.JsonHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.InputStream;
import java.lang.ref.WeakReference;
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

public abstract class CandyBarSplashActivity extends AppCompatActivity implements SplashScreenCallback {

    private AsyncTask mSplashScreenLoader;
    private AsyncTask mCloudWallpapersLoader;
    private SplashScreenConfiguration mConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mConfig = onInit();
        initBottomText();

        mSplashScreenLoader = new SplashScreenLoader(this)
                .mainActivity(mConfig.getMainActivity())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mCloudWallpapersLoader = new CloudWallpapersLoader(this).execute();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        if (mCloudWallpapersLoader != null) {
            mCloudWallpapersLoader.cancel(true);
        }
        Database.get(this.getApplicationContext()).closeDatabase();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mSplashScreenLoader != null) {
            mSplashScreenLoader.cancel(true);
        }
        super.onDestroy();
    }

    private void initBottomText() {
        TextView splashTitle = findViewById(R.id.splash_title);
        if (splashTitle != null) {
            splashTitle.setText(mConfig.getBottomText());

            if (mConfig.getBottomTextColor() != -1) {
                splashTitle.setTextColor(mConfig.getBottomTextColor());
            } else {
                int color = ContextCompat.getColor(this, R.color.splashColor);
                splashTitle.setTextColor(ColorHelper.getBodyTextColor(color));
            }

            splashTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, mConfig.getBottomTextSize());
            splashTitle.setTypeface(mConfig.getBottomTextFont(this));
        }
    }

    private static class SplashScreenLoader extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<Context> context;
        private Class<?> mainActivity;

        private SplashScreenLoader(@NonNull Context context) {
            this.context = new WeakReference<>(context);
        }

        private SplashScreenLoader mainActivity(@NonNull Class<?> mainActivity) {
            this.mainActivity = mainActivity;
            return this;
        }

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
            if (context.get() == null) return;
            if (context.get() instanceof Activity) {
                if (((Activity) context.get()).isFinishing()) return;
            }

            Intent intent = new Intent(context.get(), mainActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Activity activity = (Activity) context.get();
            activity.startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            activity.finish();
        }
    }

    private static class CloudWallpapersLoader extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<Context> context;

        private CloudWallpapersLoader(@NonNull Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    if (WallpaperHelper.getWallpaperType(context.get()) != WallpaperHelper.CLOUD_WALLPAPERS)
                        return true;

                    if (Database.get(context.get().getApplicationContext()).getWallpapersCount() > 0)
                        return true;

                    URL url = new URL(context.get().getString(R.string.wallpaper_json));
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

                        if (Database.get(context.get().getApplicationContext()).getWallpapersCount() > 0) {
                            Database.get(context.get().getApplicationContext()).deleteWallpapers();
                        }

                        Database.get(context.get().getApplicationContext()).addWallpapers(list);

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
    }
}

