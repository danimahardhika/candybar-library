package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.fragments.dialog.WallpaperSettingsFragment;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.utils.Animator;
import com.kogitune.activitytransition.ActivityTransition;
import com.kogitune.activitytransition.ExitActivityTransition;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.WallpapersAdapter;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.PermissionHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.senab.photoview.PhotoViewAttacher;

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

public class CandyBarWallpaperActivity extends AppCompatActivity implements View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private ImageView mWallpaper;
    private FloatingActionButton mFab;
    private ProgressBar mProgress;

    private String mUrl;
    private String mName;
    private String mAuthor;
    private int mColor;
    private boolean mIsEnter;

    private Runnable mRunnable;
    private Handler mHandler;
    private PhotoViewAttacher mAttacher;
    private ExitActivityTransition mExitTransition;

    private static final String URL = "url";
    private static final String NAME = "name";
    private static final String AUTHOR = "author";

    public void initWallpaperActivity(Bundle savedInstanceState, Intent intent) {
        super.setTheme(Preferences.getPreferences(this).isDarkTheme() ?
                R.style.WallpaperThemeDark : R.style.WallpaperTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        mIsEnter = true;

        mWallpaper = (ImageView) findViewById(R.id.wallpaper);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        ViewHelper.resetNavigationBarTranslucent(this, getResources().getConfiguration().orientation);

        ColorHelper.setTransparentStatusBar(this,
                ContextCompat.getColor(this, R.color.wallpaperStatusBar));
        mColor = ColorHelper.getAttributeColor(this, R.attr.colorAccent);

        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString(URL);
            mName = savedInstanceState.getString(NAME);
            mAuthor = savedInstanceState.getString(AUTHOR);
        }

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mUrl = bundle.getString(URL);
                mName = bundle.getString(NAME);
                mAuthor = bundle.getString(AUTHOR);
            }
        }

        toolbarTitle.setText(mName);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        setSupportActionBar(toolbar);

        mAttacher = new PhotoViewAttacher(mWallpaper);
        mFab.setOnClickListener(this);

        mExitTransition = ActivityTransition.with(getIntent())
                .to(this, mWallpaper, "image")
                .duration(300)
                .start(savedInstanceState);

        Animator.startAlphaAnimation(toolbar, 1000, View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && savedInstanceState == null) {
            Transition transition = getWindow().getSharedElementEnterTransition();

            if (transition != null) {
                transition.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        if (mIsEnter) {
                            mIsEnter = false;
                            loadWallpaper(mUrl);
                        }
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });
                return;
            }
        }

        mRunnable = () -> loadWallpaper(mUrl);
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 700);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarTranslucent(this, newConfig.orientation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wallpaper, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(NAME, mName);
        outState.putString(AUTHOR, mAuthor);
        outState.putString(URL, mUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (mAttacher != null) mAttacher.cleanup();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        WallpapersAdapter.sIsClickable = true;
        if (mHandler != null && mRunnable != null)
            mHandler.removeCallbacks(mRunnable);
        if (mExitTransition != null) mExitTransition.exit(this);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_save) {
            if (PermissionHelper.isPermissionStorageGranted(this)) {
                WallpaperHelper.downloadWallpaper(this, mColor, mUrl, mName, R.id.coordinator_layout);
            } else {
                PermissionHelper.requestStoragePermission(this, PermissionHelper.PERMISSION_STORAGE);
            }
            return true;
        } else if (id == R.id.menu_wallpaper_settings) {
            WallpaperSettingsFragment.showWallpaperSettings(getSupportFragmentManager());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fab) {
            WallpaperHelper.applyWallpaper(this, mAttacher.getDisplayRect(), mColor, mUrl, mName);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.PERMISSION_STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Preferences.getPreferences(this).setCacheAllowed(true);
                recreate();
            } else {
                PermissionHelper.showPermissionStorageDenied(this);
            }
        }
    }

    private void loadWallpaper(String url) {
        ImageLoader.getInstance().handleSlowNetwork(true);
        ImageLoader.getInstance().displayImage(url, mWallpaper, ImageConfig.getWallpaperPreviewOptions(
                Preferences.getPreferences(this).isCacheAllowed()), new SimpleImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                int text = ColorHelper.getTitleTextColor(CandyBarWallpaperActivity.this, mColor);
                OnWallpaperLoaded(text);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                if (!Preferences.getPreferences(CandyBarWallpaperActivity.this).isScrollWallpaper()) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

                if (loadedImage != null) {
                    Palette.from(loadedImage).generate(palette -> {
                        int accent = ColorHelper.getAttributeColor(
                                CandyBarWallpaperActivity.this, R.attr.colorAccent);
                        int color = palette.getVibrantColor(accent);
                        mColor = color;
                        int text = ColorHelper.getTitleTextColor(CandyBarWallpaperActivity.this, color);
                        mFab.setBackgroundTintList(ColorHelper.getColorStateList(
                                android.R.attr.state_pressed,
                                color, ColorHelper.getDarkerColor(color, 0.9f)));
                        OnWallpaperLoaded(text);
                    });
                }
            }
        });
    }

    private void OnWallpaperLoaded(@ColorInt int textColor) {
        mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mProgress.setVisibility(View.GONE);
        mRunnable = null;
        mHandler = null;

        mFab.setImageDrawable(DrawableHelper.getTintedDrawable(
                CandyBarWallpaperActivity.this, R.drawable.ic_fab_check, textColor));
        mFab.show();
        mFab.setVisibility(View.VISIBLE);
    }

}
