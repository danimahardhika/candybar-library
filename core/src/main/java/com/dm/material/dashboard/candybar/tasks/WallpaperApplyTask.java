package com.dm.material.dashboard.candybar.tasks;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.TypefaceHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.Locale;
import java.util.concurrent.Executor;

/*
 * Wallpaper Board
 *
 * Copyright (c) 2017 Dani Mahardhika
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

public class WallpaperApplyTask extends AsyncTask<Void, Void, Boolean> implements WallpaperPropertiesLoaderTask.Callback{

    private final Context mContext;
    private Apply mApply;
    private RectF mRectF;
    private Executor mExecutor;
    private Wallpaper mWallpaper;
    private MaterialDialog mDialog;

    private WallpaperApplyTask(Context context) {
        mContext = context;
    }

    public WallpaperApplyTask to(Apply apply) {
        mApply = apply;
        return this;
    }

    public WallpaperApplyTask wallpaper(@NonNull Wallpaper wallpaper) {
        mWallpaper = wallpaper;
        return this;
    }

    public WallpaperApplyTask crop(@Nullable RectF rectF) {
        mRectF = rectF;
        return this;
    }

    public AsyncTask start() {
        return start(SERIAL_EXECUTOR);
    }

    public AsyncTask start(@NonNull Executor executor) {
        if (mDialog == null) {
            int color = mWallpaper.getColor();
            if (color == 0) {
                color = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);
            }

            final MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
            builder.widgetColor(color)
                    .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                    .progress(true, 0)
                    .cancelable(false)
                    .progressIndeterminateStyle(true)
                    .content(R.string.wallpaper_loading)
                    .positiveColor(color)
                    .positiveText(android.R.string.cancel)
                    .onPositive((dialog, which) -> {
                        ImageLoader.getInstance().stop();
                        cancel(true);
                    });

            mDialog = builder.build();
        }

        if (!mDialog.isShowing()) mDialog.show();

        mExecutor = executor;
        if (mWallpaper == null) {
            LogUtil.e("WallpaperApply cancelled, wallpaper is null");
            return null;
        }

        if (mWallpaper.getDimensions() == null) {
            return WallpaperPropertiesLoaderTask.prepare(mContext)
                    .wallpaper(mWallpaper)
                    .callback(this)
                    .start(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        return executeOnExecutor(executor);
    }

    public static WallpaperApplyTask prepare(@NonNull Context context) {
        return new WallpaperApplyTask(context);
    }

    @Override
    public void onPropertiesReceived(Wallpaper wallpaper) {
        mWallpaper = wallpaper;
        if (mExecutor == null) mExecutor = SERIAL_EXECUTOR;
        if (mWallpaper.getDimensions() == null) {
            mDialog.dismiss();
            LogUtil.e("WallpaperApply cancelled, unable to retrieve wallpaper dimensions");

            Toast.makeText(mContext, R.string.wallpaper_apply_failed,
                    Toast.LENGTH_LONG).show();
            return;
        }

        start(mExecutor);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                Thread.sleep(1);
                ImageSize imageSize = WallpaperHelper.getTargetSize(mContext);

                LogUtil.d("original rectF: " +mRectF);

                if (mRectF != null && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                    Point point = WindowHelper.getScreenSize(mContext);
                    int height = point.y - WindowHelper.getStatusBarHeight(mContext) - WindowHelper.getNavigationBarHeight(mContext);
                    float heightFactor = (float) imageSize.getHeight() / (float) height;
                    mRectF = WallpaperHelper.getScaledRectF(mRectF, heightFactor, 1f);
                }

                if (mRectF == null && Preferences.get(mContext).isCropWallpaper()) {
                    /*
                     * Create a center crop rectF if wallpaper applied from grid, not opening the preview first
                     */
                    float widthScaleFactor = (float) imageSize.getHeight() / (float) mWallpaper.getDimensions().getHeight();

                    float side = ((float) mWallpaper.getDimensions().getWidth() * widthScaleFactor - (float) imageSize.getWidth())/2f;
                    float leftRectF = 0f - side;
                    float rightRectF = (float) mWallpaper.getDimensions().getWidth() * widthScaleFactor - side;
                    float topRectF = 0f;
                    float bottomRectF = (float) imageSize.getHeight();
                    mRectF = new RectF(leftRectF, topRectF, rightRectF, bottomRectF);
                    LogUtil.d("created center crop rectF: " +mRectF);
                }

                ImageSize adjustedSize = imageSize;
                RectF adjustedRectF = mRectF;

                float scaleFactor = (float) mWallpaper.getDimensions().getHeight() / (float) imageSize.getHeight();
                if (scaleFactor > 1f) {
                    /*
                     * Applying original wallpaper size caused a problem (wallpaper zoomed in)
                     * if wallpaper dimension bigger than device screen resolution
                     *
                     * Solution: Resize wallpaper to match screen resolution
                     */

                    /*
                     * Use original wallpaper size:
                     * adjustedSize = new ImageSize(width, height);
                     */

                    /*
                     * Adjust wallpaper size to match screen resolution:
                     */
                    float widthScaleFactor = (float) imageSize.getHeight() / (float) mWallpaper.getDimensions().getHeight();
                    int adjustedWidth = Float.valueOf((float) mWallpaper.getDimensions().getWidth() * widthScaleFactor).intValue();
                    adjustedSize = new ImageSize(adjustedWidth, imageSize.getHeight());

                    if (adjustedRectF != null) {
                        /*
                         * If wallpaper crop enabled, original wallpaper size should be loaded first
                         */
                        adjustedSize = new ImageSize(mWallpaper.getDimensions().getWidth(), mWallpaper.getDimensions().getHeight());
                        adjustedRectF = WallpaperHelper.getScaledRectF(mRectF, scaleFactor, scaleFactor);
                        LogUtil.d("adjusted rectF: " + adjustedRectF);
                    }

                    LogUtil.d(String.format(Locale.getDefault(), "adjusted bitmap: %d x %d",
                            adjustedSize.getWidth(), adjustedSize.getHeight()));
                }

                int call = 1;
                do {
                    /*
                     * Load the bitmap first
                     */
                    Bitmap loadedBitmap = ImageLoader.getInstance().loadImageSync(
                            mWallpaper.getURL(), adjustedSize, ImageConfig.getWallpaperOptions());
                    if (loadedBitmap != null) {
                        try {
                            /*
                             * Checking if loaded bitmap resolution supported by the device
                             * If texture size too big then resize it
                             */
                            Bitmap bitmapTemp = Bitmap.createBitmap(
                                    loadedBitmap.getWidth(),
                                    loadedBitmap.getHeight(),
                                    loadedBitmap.getConfig());
                            bitmapTemp.recycle();

                            /*
                             * Texture size is ok
                             */
                            LogUtil.d(String.format(Locale.getDefault(), "loaded bitmap: %d x %d",
                                    loadedBitmap.getWidth(), loadedBitmap.getHeight()));
                            publishProgress();

                            Bitmap bitmap = loadedBitmap;
                            WallpaperManager manager = WallpaperManager.getInstance(mContext);
                            if (Preferences.get(mContext).isCropWallpaper() && adjustedRectF != null) {
                                LogUtil.d("rectF: " +adjustedRectF);
                                /*
                                 * Cropping bitmap
                                 */
                                ImageSize targetSize = WallpaperHelper.getTargetSize(mContext);

                                int targetWidth = Double.valueOf(
                                        ((double) loadedBitmap.getHeight() / (double) targetSize.getHeight())
                                                * (double) targetSize.getWidth()).intValue();

                                bitmap = Bitmap.createBitmap(
                                        targetWidth,
                                        loadedBitmap.getHeight(),
                                        loadedBitmap.getConfig());
                                Paint paint = new Paint();
                                paint.setFilterBitmap(true);
                                paint.setAntiAlias(true);
                                paint.setDither(true);

                                Canvas canvas = new Canvas(bitmap);
                                canvas.drawBitmap(loadedBitmap, null, adjustedRectF, paint);

                                float scale = (float) targetSize.getHeight() / (float) bitmap.getHeight();
                                if (scale < 1f) {
                                    LogUtil.d("bitmap size is bigger than screen resolution, resizing bitmap");
                                    int resizedWidth = Float.valueOf((float) bitmap.getWidth() * scale).intValue();
                                    bitmap = Bitmap.createScaledBitmap(bitmap, resizedWidth, targetSize.getHeight(), true);
                                }
                            }

                            /*
                             * Final bitmap generated
                             */
                            LogUtil.d(String.format(Locale.getDefault(), "generated bitmap: %d x %d ",
                                    bitmap.getWidth(), bitmap.getHeight()));

                            if (mApply == Apply.HOMESCREEN) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                                    return true;
                                }

                                manager.setBitmap(bitmap);
                                return true;
                            }

                            if (mApply == Apply.LOCKSCREEN) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                                    return true;
                                }
                            }
                        } catch (OutOfMemoryError e) {
                            LogUtil.e("loaded bitmap is too big, resizing it ...");
                            /*
                             * Texture size is too big
                             * Resizing bitmap
                             */

                            double scale = 1 - (0.1 * call);
                            int scaledWidth = Double.valueOf(adjustedSize.getWidth() * scale).intValue();
                            int scaledHeight = Double.valueOf(adjustedSize.getHeight() * scale).intValue();

                            adjustedRectF = WallpaperHelper.getScaledRectF(adjustedRectF,
                                    (float) scale, (float) scale);
                            adjustedSize = new ImageSize(scaledWidth, scaledHeight);
                        }
                    }

                    /*
                     * Continue to next iteration
                     */
                    call++;
                } while (call <= 5 && !isCancelled());
                return false;
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        mDialog.setContent(R.string.wallpaper_applying);
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        Toast.makeText(mContext, R.string.wallpaper_apply_cancelled,
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (mContext == null) {
            return;
        }

        if (((AppCompatActivity) mContext).isFinishing()) {
            return;
        }

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (aBoolean) {
            CafeBar.builder(mContext)
                    .theme(CafeBarTheme.Custom(ColorHelper.getAttributeColor(
                            mContext, R.attr.card_background)))
                    .contentTypeface(TypefaceHelper.getRegular(mContext))
                    .floating(true)
                    .fitSystemWindow()
                    .content(R.string.wallpaper_applied)
                    .show();
        } else {
            Toast.makeText(mContext, R.string.wallpaper_apply_failed,
                    Toast.LENGTH_LONG).show();
        }
    }

    public enum Apply {
        LOCKSCREEN,
        HOMESCREEN
    }
}
