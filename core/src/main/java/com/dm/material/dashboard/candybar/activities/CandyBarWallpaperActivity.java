package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.danimahardhika.android.helpers.permission.PermissionCode;
import com.danimahardhika.android.helpers.permission.PermissionHelper;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.helpers.LocaleHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.WallpapersAdapter;
import com.dm.material.dashboard.candybar.helpers.TapIntroHelper;
import com.dm.material.dashboard.candybar.items.PopupItem;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.tasks.WallpaperApplyTask;
import com.dm.material.dashboard.candybar.tasks.WallpaperPropertiesLoaderTask;
import com.dm.material.dashboard.candybar.utils.Extras;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.Popup;
import com.dm.material.dashboard.candybar.utils.WallpaperDownloader;
import com.kogitune.activitytransition.ActivityTransition;
import com.kogitune.activitytransition.ExitActivityTransition;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
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

public class CandyBarWallpaperActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, WallpaperPropertiesLoaderTask.Callback {

    private ImageView mImageView;
    private ProgressBar mProgress;
    private LinearLayout mBottomBar;
    private TextView mName;
    private TextView mAuthor;
    private ImageView mBack;
    private ImageView mMenuApply;
    private ImageView mMenuSave;

    private boolean mIsEnter;
    private boolean mIsResumed = false;

    private Wallpaper mWallpaper;
    private Runnable mRunnable;
    private Handler mHandler;
    private PhotoViewAttacher mAttacher;
    private ExitActivityTransition mExitTransition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.setTheme(Preferences.get(this).isDarkTheme() ?
                R.style.WallpaperThemeDark : R.style.WallpaperTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        mIsEnter = true;

        mImageView = findViewById(R.id.wallpaper);
        mProgress = findViewById(R.id.progress);
        mBottomBar = findViewById(R.id.bottom_bar);
        mName = findViewById(R.id.name);
        mAuthor = findViewById(R.id.author);
        mBack = findViewById(R.id.back);
        mMenuApply = findViewById(R.id.menu_apply);
        mMenuSave = findViewById(R.id.menu_save);

        mProgress.getIndeterminateDrawable().setColorFilter(
                Color.parseColor("#CCFFFFFF"), PorterDuff.Mode.SRC_IN);
        mBack.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_back, Color.WHITE));
        mBack.setOnClickListener(this);

        String url = "";
        if (savedInstanceState != null) {
            url = savedInstanceState.getString(Extras.EXTRA_URL);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            url = bundle.getString(Extras.EXTRA_URL);
        }

        mWallpaper = Database.get(this.getApplicationContext()).getWallpaper(url);
        if (mWallpaper == null) {
            finish();
            return;
        }

        initBottomBar();
        resetBottomBarPadding();

        if (!mIsResumed) {
            mExitTransition = ActivityTransition
                    .with(getIntent())
                    .to(this, mImageView, Extras.EXTRA_IMAGE)
                    .duration(300)
                    .start(savedInstanceState);
        }

        if (mImageView.getDrawable() == null) {
            int color = mWallpaper.getColor();
            if (color == 0) {
                color = ColorHelper.getAttributeColor(this, R.attr.card_background);
            }

            AnimationHelper.setBackgroundColor(findViewById(R.id.rootview), Color.TRANSPARENT, color).start();
            mProgress.getIndeterminateDrawable().setColorFilter(
                    ColorHelper.setColorAlpha(ColorHelper.getTitleTextColor(color), 0.7f),
                    PorterDuff.Mode.SRC_IN);
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

                            AnimationHelper.fade(mBottomBar).duration(400).start();
                            loadWallpaper();
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
            AnimationHelper.fade(mBottomBar).duration(400).start();
            loadWallpaper();

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
        resetBottomBarPadding();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mWallpaper != null) {
            outState.putString(Extras.EXTRA_URL, mWallpaper.getURL());
        }

        outState.putBoolean(Extras.EXTRA_RESUMED, true);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        ImageLoader.getInstance().cancelDisplayTask(mImageView);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back) {
            onBackPressed();
        } else if (id == R.id.menu_apply) {
            Popup popup = Popup.Builder(this)
                    .to(mMenuApply)
                    .list(PopupItem.getApplyItems(this))
                    .callback((p, position) -> {
                        PopupItem item = p.getItems().get(position);
                        if (item.getType() == PopupItem.Type.WALLPAPER_CROP) {
                            Preferences.get(this).setCropWallpaper(!item.getCheckboxValue());
                            item.setCheckboxValue(Preferences.get(this).isCropWallpaper());

                            p.updateItem(position, item);
                            if (Preferences.get(this).isCropWallpaper()) {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                return;
                            }

                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                            return;
                        } else if (item.getType() == PopupItem.Type.LOCKSCREEN) {
                            RectF rectF = null;
                            if (Preferences.get(this).isCropWallpaper()) {
                                if (mAttacher != null)
                                    rectF = mAttacher.getDisplayRect();
                            }

                            WallpaperApplyTask.prepare(this)
                                    .wallpaper(mWallpaper)
                                    .to(WallpaperApplyTask.Apply.LOCKSCREEN)
                                    .crop(rectF)
                                    .start(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else if (item.getType() == PopupItem.Type.HOMESCREEN) {
                            RectF rectF = null;
                            if (Preferences.get(this).isCropWallpaper()) {
                                if (mAttacher != null)
                                    rectF = mAttacher.getDisplayRect();
                            }

                            WallpaperApplyTask.prepare(this)
                                    .wallpaper(mWallpaper)
                                    .to(WallpaperApplyTask.Apply.HOMESCREEN)
                                    .crop(rectF)
                                    .start(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                        p.dismiss();
                    })
                    .build();

            if (getResources().getBoolean(R.bool.enable_wallpaper_download)) {
                popup.removeItem(popup.getItems().size() - 1);
            }
            popup.show();
        } else if (id == R.id.menu_save) {
            if (PermissionHelper.isStorageGranted(this)) {
                WallpaperDownloader.prepare(this)
                        .wallpaper(mWallpaper)
                        .start();
                return;
            }

            PermissionHelper.requestStorage(this);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        int res = 0;
        if (id == R.id.menu_apply) {
            res = R.string.wallpaper_apply;
        } else if (id == R.id.menu_save) {
            res = R.string.wallpaper_save_to_device;
        }

        if (res == 0) return false;

        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCode.STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                WallpaperDownloader.prepare(this).wallpaper(mWallpaper).start();
            } else {
                Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPropertiesReceived(Wallpaper wallpaper) {
        if (wallpaper == null) return;

        mWallpaper.setDimensions(wallpaper.getDimensions());
        mWallpaper.setSize(wallpaper.getSize());
        mWallpaper.setMimeType(wallpaper.getMimeType());
    }

    private void initBottomBar() {
        mName.setText(mWallpaper.getName());
        mName.setTextColor(Color.WHITE);
        mAuthor.setText(mWallpaper.getAuthor());
        mAuthor.setTextColor(ColorHelper.setColorAlpha(Color.WHITE, 0.7f));
        mMenuSave.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_download, Color.WHITE));
        mMenuApply.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_apply_options, Color.WHITE));

        if (getResources().getBoolean(R.bool.enable_wallpaper_download)) {
            mMenuSave.setVisibility(View.VISIBLE);
        }

        mMenuApply.setOnClickListener(this);
        mMenuSave.setOnClickListener(this);

        mMenuApply.setOnLongClickListener(this);
        mMenuSave.setOnLongClickListener(this);
    }

    private void resetBottomBarPadding() {
        LinearLayout container = findViewById(R.id.bottom_bar_container);
        int height = getResources().getDimensionPixelSize(R.dimen.bottom_bar_height);
        int bottom = 0;
        int right = WindowHelper.getNavigationBarHeight(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBack.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mBack.getLayoutParams();
                params.topMargin = WindowHelper.getStatusBarHeight(this);
            }

            boolean tabletMode = getResources().getBoolean(R.bool.android_helpers_tablet_mode);
            if (tabletMode || getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                bottom = right;
                right = 0;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (isInMultiWindowMode()) {
                    bottom = right = 0;
                }
            }
        }

        container.setPadding(0, 0, right, bottom);

        if (container.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
            params.height = height + bottom;
        }
    }

    private void loadWallpaper() {
        if (mAttacher != null) {
            mAttacher.cleanup();
            mAttacher = null;
        }

        WallpaperPropertiesLoaderTask.prepare(this)
                .callback(this)
                .wallpaper(mWallpaper)
                .start(AsyncTask.THREAD_POOL_EXECUTOR);

        DisplayImageOptions.Builder options = ImageConfig.getRawDefaultImageOptions();
        options.cacheInMemory(false);
        options.cacheOnDisk(true);

        ImageLoader.getInstance().handleSlowNetwork(true);
        ImageLoader.getInstance().displayImage(mWallpaper.getURL(), mImageView, options.build(), new SimpleImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                if (Preferences.get(CandyBarWallpaperActivity.this).isCropWallpaper()) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

                AnimationHelper.fade(mProgress).start();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                if (mWallpaper.getColor() == 0) {
                    mWallpaper.setColor(ColorHelper.getAttributeColor(
                            CandyBarWallpaperActivity.this, R.attr.colorAccent));
                }

                onWallpaperLoaded();
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);

                if (loadedImage != null && mWallpaper.getColor() == 0) {
                    Palette.from(loadedImage).generate(palette -> {
                        int accent = ColorHelper.getAttributeColor(
                                CandyBarWallpaperActivity.this, R.attr.colorAccent);
                        int color = palette.getVibrantColor(accent);
                        if (color == accent)
                            color = palette.getMutedColor(accent);

                        mWallpaper.setColor(color);
                        Database.get(CandyBarWallpaperActivity.this).updateWallpaper(mWallpaper);

                        onWallpaperLoaded();
                    });
                    return;
                }

                onWallpaperLoaded();
            }
        }, null);
    }

    private void onWallpaperLoaded() {
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);

        AnimationHelper.fade(mProgress).start();
        mRunnable = null;
        mHandler = null;
        mIsResumed = false;

        TapIntroHelper.showWallpaperPreviewIntro(this, mWallpaper.getColor());
    }
}
