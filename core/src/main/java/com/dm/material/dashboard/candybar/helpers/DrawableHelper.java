package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.utils.LogUtil;

import java.io.ByteArrayOutputStream;

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

public class DrawableHelper {

    public static int getResourceId(@NonNull Context context, String resName) {
        try {
            return context.getResources().getIdentifier(
                    resName, "drawable", context.getPackageName());
        } catch (Exception ignored) {}
        return -1;
    }

    @Nullable
    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int res) {
        try {
            Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, res);
            return drawable.mutate();
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    @Nullable
    public static Drawable getTintedDrawable(@NonNull Context context, @DrawableRes int res, @ColorInt int color) {
        try {
            Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, res);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            return drawable.mutate();
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    @Nullable
    public static Drawable getResizedDrawable(@NonNull Context context, @DrawableRes int drawableRes, @DimenRes int dimenRes) {
        try {
            Drawable drawable = getDrawable(context, drawableRes);
            if (drawable == null) return null;

            int size = context.getResources().getDimensionPixelSize(dimenRes);

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return new BitmapDrawable(context.getResources(),
                    Bitmap.createScaledBitmap(bitmap, size, size, true));
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.d(Log.getStackTraceString(e));
            return null;
        }
    }

    @Nullable
    public static Drawable getDefaultImage(@NonNull Context context, @DrawableRes int res,
                                           @ColorInt int color, int padding) {
        try {
            Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, res);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            Bitmap tintedBitmap = Bitmap.createBitmap(
                    bitmap.getWidth() + padding,
                    bitmap.getHeight() + padding,
                    Bitmap.Config.ARGB_8888);
            Canvas tintedCanvas = new Canvas(tintedBitmap);
            int background = ColorHelper.getAttributeColor(context, R.attr.card_background);
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            tintedCanvas.drawColor(background, PorterDuff.Mode.ADD);
            tintedCanvas.drawBitmap(bitmap,
                    (tintedCanvas.getWidth() - bitmap.getWidth())/2,
                    (tintedCanvas.getHeight() - bitmap.getHeight())/2, paint);
            return new BitmapDrawable(context.getResources(), tintedBitmap);
        } catch (Exception | OutOfMemoryError e) {
            return null;
        }
    }

    public static Drawable getAppIcon(@NonNull Context context, ResolveInfo info) {
        try {
            return info.activityInfo.loadIcon(context.getPackageManager());
        } catch (OutOfMemoryError | Exception e) {
            return ContextCompat.getDrawable(context, R.drawable.ic_app_default);
        }
    }

    @Nullable
    public static Bitmap getHighQualityIcon(@NonNull Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(
                    packageName, PackageManager.GET_META_DATA);

            Resources resources = packageManager.getResourcesForApplication(packageName);
            int density = DisplayMetrics.DENSITY_XXHIGH;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                density = DisplayMetrics.DENSITY_XXXHIGH;
            }

            Drawable drawable = ResourcesCompat.getDrawableForDensity(
                    resources, info.icon, density, null);
            if (drawable != null) return ((BitmapDrawable) drawable).getBitmap();
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }

    @Nullable
    public static byte[] getBitmapByte(@NonNull Drawable drawable) {
        try {
            Bitmap bitmap;
            if (drawable instanceof LayerDrawable) {
                bitmap = Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                drawable.draw(new Canvas(bitmap));
            } else {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, stream);
            return stream.toByteArray();
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }
}

