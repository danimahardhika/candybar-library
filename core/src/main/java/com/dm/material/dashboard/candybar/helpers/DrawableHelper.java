package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.Log;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.utils.Tag;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
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
        return context.getResources().getIdentifier(
                resName, "drawable", context.getPackageName());
    }

    @NonNull
    public static Drawable getTintedDrawable(@NonNull Context context, @DrawableRes int res, @ColorInt int color) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, res);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable.mutate();
    }

    public static Drawable getAppIcon(@NonNull Context context, ResolveInfo info) {
        try {
            return info.activityInfo.loadIcon(context.getPackageManager());
        } catch (OutOfMemoryError | Exception e) {
            return ContextCompat.getDrawable(context, R.drawable.ic_app_default);
        }
    }

    @Nullable
    public static Bitmap getBitmap(@NonNull Drawable drawable) {
        try {
            if (drawable instanceof LayerDrawable) {
                Bitmap bitmap = Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                drawable.draw(new Canvas(bitmap));
                return bitmap;
            }
            return ((BitmapDrawable) drawable).getBitmap();
        } catch (Exception | OutOfMemoryError e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
        return null;
    }

}

