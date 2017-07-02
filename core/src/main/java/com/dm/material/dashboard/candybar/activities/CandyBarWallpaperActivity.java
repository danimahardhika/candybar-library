package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.permission.PermissionCode;
import com.danimahardhika.android.helpers.permission.PermissionHelper;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.material.dashboard.candybar.fragments.dialog.WallpaperSettingsFragment;
import com.dm.material.dashboard.candybar.helpers.LocaleHelper;
import com.dm.material.dashboard.candybar.helpers.TapIntroHelper;
import com.dm.material.dashboard.candybar.helpers.TypefaceHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.WallpapersAdapter;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.kogitune.activitytransition.ActivityTransition;
import com.kogitune.activitytransition.ExitActivityTransition;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;

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
    private ProgressBar mBottomProgress;

    private String mUrl;
    private String mName;
    private String mAuthor;
    private int mColor;
    private boolean mIsEnter;
    private boolean mIsResumed = false;

    private Runnable mRunnable;
    private Handler mHandler;
    private PhotoViewAttacher mAttacher;
    private ExitActivityTransition mExitTransition;

    private static final String URL = "url";
    private static final String NAME = "name";
    private static final String AUTHOR = "author";
    private static final String RESUMED = "resumed";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.setTheme(Preferences.get(this).isDarkTheme() ?
                R.style.WallpaperThemeDark : R.style.WallpaperTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        mIsEnter = true;

        mWallpaper = (ImageView) findViewById(R.id.wallpaper);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mBottomProgress = (ProgressBar) findViewById(R.id.bottom_progress);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        TextView toolbarSubTitle = (TextView) findViewById(R.id.toolbar_subtitle);

        ViewHelper.resetViewBottomMargin(mFab);

        mProgress.getIndeterminateDrawable().setColorFilter(
                Color.parseColor("#CCFFFFFF"), PorterDuff.Mode.SRC_IN);
        mBottomProgress.getProgressDrawable().setColorFilter(
                Color.parseColor("#EEFFFFFF"), PorterDuff.Mode.SRC_IN);
        ColorHelper.setStatusBarColor(this,
                ContextCompat.getColor(this, R.color.wallpaperStatusBar), true);
        mColor = ColorHelper.getAttributeColor(this, R.attr.colorAccent);

        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString(URL);
            mName = savedInstanceState.getString(NAME);
            mAuthor = savedInstanceState.getString(AUTHOR);
            mIsResumed = savedInstanceState.getBoolean(RESUMED);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mUrl = bundle.getString(URL);
            mName = bundle.getString(NAME);
            mAuthor = bundle.getString(AUTHOR);
        }

        toolbarTitle.setText(mName);
        toolbarSubTitle.setText(mAuthor);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        setSupportActionBar(toolbar);

        mFab.setOnClickListener(this);
        if (!Preferences.get(this).isShadowEnabled()) {
            mFab.setCompatElevation(0f);
        }

        if (!mIsResumed) {
            mExitTransition = ActivityTransition
                    .with(getIntent())
                    .to(this, mWallpaper, "image")
                    .duration(300)
                    .start(savedInstanceState);
        }

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
                            AnimationHelper.slideDownIn(toolbar)
                                    .duration(300)
                                    .interpolator(new LinearOutSlowInInterpolator())
                                    .start();

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

        mRunnable = () -> {
            toolbar.setVisibility(View.VISIBLE);
            loadWallpaper(mUrl);
            mRunnable = null;
            mHandler = null;
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 700);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.setLocale(this);
        ViewHelper.resetViewBottomMargin(mFab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wallpaper, menu);
        MenuItem save = menu.findItem(R.id.menu_save);
        save.setVisible(getResources().getBoolean(R.bool.enable_wallpaper_download));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(NAME, mName);
        outState.putString(AUTHOR, mAuthor);
        outState.putString(URL, mUrl);
        outState.putBoolean(RESUMED, true);
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

        if (mExitTransition != null) {
            mExitTransition.exit(this);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_save) {
            if (PermissionHelper.isStorageGranted(this)) {
                File target = new File(WallpaperHelper.getDefaultWallpapersDirectory(this).toString()
                        + File.separator + mName + WallpaperHelper.IMAGE_EXTENSION);

                if (target.exists()) {
                    CafeBar.builder(this)
                            .theme(new CafeBarTheme.Custom(ColorHelper.getAttributeColor(this, R.attr.card_background)))
                            .autoDismiss(false)
                            .maxLines(4)
                            .fitSystemWindow()
                            .typeface(TypefaceHelper.getRegular(this), TypefaceHelper.getBold(this))
                            .content(String.format(getResources().getString(R.string.wallpaper_download_exist),
                                    ("\"" +mName + WallpaperHelper.IMAGE_EXTENSION+ "\"")))
                            .icon(R.drawable.ic_toolbar_download)
                            .positiveText(R.string.wallpaper_download_exist_replace)
                            .positiveColor(mColor)
                            .onPositive(cafeBar -> {
                                WallpaperHelper.downloadWallpaper(this, mColor, mUrl, mName);
                                cafeBar.dismiss();
                            })
                            .negativeText(R.string.wallpaper_download_exist_new)
                            .onNegative(cafeBar -> {
                                WallpaperHelper.downloadWallpaper(this, mColor, mUrl, mName +"_"+ System.currentTimeMillis());
                                cafeBar.dismiss();
                            })
                            .build().show();
                    return true;
                }

                WallpaperHelper.downloadWallpaper(this, mColor, mUrl, mName);
                return true;
            }

            PermissionHelper.requestStorage(this);
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
            RectF rectF = mAttacher.getDisplayRect();
            WallpaperHelper.applyWallpaper(this,
                    Preferences.get(this).isWallpaperCrop() ? rectF : null,
                    mColor, mUrl, mName);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCode.STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                WallpaperHelper.downloadWallpaper(this, mColor, mUrl, mName);
            } else {
                Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadWallpaper(String url) {
        DisplayImageOptions.Builder options = ImageConfig.getRawDefaultImageOptions();
        options.cacheInMemory(false);
        options.cacheOnDisk(true);

        ImageLoader.getInstance().handleSlowNetwork(true);
        ImageLoader.getInstance().displayImage(url, mWallpaper, options.build(), new SimpleImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                if (Preferences.get(CandyBarWallpaperActivity.this).isWallpaperCrop()) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

                AnimationHelper.fade(mProgress).start();
                AnimationHelper.fade(mBottomProgress).start();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                onWallpaperLoaded();
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);

                if (loadedImage != null) {
                    Palette.from(loadedImage).generate(palette -> {
                        int accent = ColorHelper.getAttributeColor(
                                CandyBarWallpaperActivity.this, R.attr.colorAccent);
                        int color = palette.getVibrantColor(accent);
                        mColor = color;
                        mFab.setBackgroundTintList(ColorHelper.getColorStateList(color));
                        onWallpaperLoaded();
                    });
                }
            }
        }, (imageUri, view, current, total) -> {
            mBottomProgress.setMax(total);
            mBottomProgress.setProgress(current);
        });
    }

    private void onWallpaperLoaded() {
        int textColor = ColorHelper.getTitleTextColor(mColor);

        mAttacher = new PhotoViewAttacher(mWallpaper);
        mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);
        AnimationHelper.fade(mProgress).start();
        AnimationHelper.fade(mBottomProgress).start();
        mRunnable = null;
        mHandler = null;
        mIsResumed = false;

        mFab.setImageDrawable(DrawableHelper.getTintedDrawable(
                CandyBarWallpaperActivity.this, R.drawable.ic_fab_apply, textColor));
        AnimationHelper.show(mFab)
                .interpolator(new LinearOutSlowInInterpolator())
                .start();

        try {
            TapIntroHelper.showWallpaperPreviewIntro(this, mColor);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }
}
