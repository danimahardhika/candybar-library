package com.dm.material.dashboard.candybar.helpers;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import com.danimahardhika.android.helpers.core.WindowHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import java.io.File;

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

public class WallpaperHelper {

    public static final int UNKNOWN = 0;
    public static final int CLOUD_WALLPAPERS = 1;
    public static final int EXTERNAL_APP = 2;

    public static final String IMAGE_EXTENSION = ".jpeg";

    public static int getWallpaperType(@NonNull Context context) {
        String url = context.getResources().getString(R.string.wallpaper_json);
        if (URLUtil.isValidUrl(url)) {
            return CLOUD_WALLPAPERS;
        } else if (url.length() > 0) {
            return EXTERNAL_APP;
        }
        return UNKNOWN;
    }

    public static void launchExternalApp(@NonNull Context context) {
        String packageName = context.getResources().getString(R.string.wallpaper_json);

        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            try {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            } catch (Exception ignored) {}
        }

        try {
            Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://play.google.com/store/apps/details?id=" + packageName));
            context.startActivity(store);
        } catch (ActivityNotFoundException ignored) {}
    }

    public static File getDefaultWallpapersDirectory(@NonNull Context context) {
        try {
            if (Preferences.get(context).getWallsDirectory().length() == 0) {
                return new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) +"/"+
                        context.getResources().getString(R.string.app_name));
            }
            return new File(Preferences.get(context).getWallsDirectory());
        } catch (Exception e) {
            return new File(context.getFilesDir().toString() +"/Pictures/"+
                    context.getResources().getString(R.string.app_name));
        }
    }

    public static String getFormat(String mimeType) {
        if (mimeType == null) return "jpg";
        switch (mimeType) {
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            default:
                return "jpg";
        }
    }

    public static boolean isWallpaperSaved(@NonNull Context context, @NonNull Wallpaper wallpaper) {
        String fileName = wallpaper.getName() + "." + getFormat(wallpaper.getMimeType());
        File directory = WallpaperHelper.getDefaultWallpapersDirectory(context);
        File target = new File(directory, fileName);

        if (target.exists()) {
            long size = target.length();
            return size == wallpaper.getSize();
        }
        return false;
    }

    public static ImageSize getTargetSize(@NonNull Context context) {
        Point point = WindowHelper.getScreenSize(context);
        int targetHeight = point.y;
        int targetWidth = point.x;

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            targetHeight = point.x;
            targetWidth = point.y;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            int statusBarHeight = WindowHelper.getStatusBarHeight(context);
            int navBarHeight = WindowHelper.getNavigationBarHeight(context);
            targetHeight += (statusBarHeight + navBarHeight);
        }
        return new ImageSize(targetWidth, targetHeight);
    }

    @Nullable
    public static RectF getScaledRectF(@Nullable RectF rectF, float heightFactor, float widthFactor) {
        if (rectF == null) return null;

        RectF scaledRectF = new RectF(rectF);
        scaledRectF.top *= heightFactor;
        scaledRectF.bottom *= heightFactor;
        scaledRectF.left *= widthFactor;
        scaledRectF.right *= widthFactor;
        return scaledRectF;
    }
}
