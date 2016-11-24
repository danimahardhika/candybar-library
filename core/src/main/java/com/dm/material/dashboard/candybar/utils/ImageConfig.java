package com.dm.material.dashboard.candybar.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;

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

public class ImageConfig {

    public static ImageLoaderConfiguration getImageLoaderConfiguration(@NonNull Context context) {
        L.writeLogs(true);
        L.writeDebugLogs(true);
        return new ImageLoaderConfiguration.Builder(context)
                .diskCacheSize(50 * 1024 * 1024)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .threadPoolSize(4)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .build();
    }

    public static DisplayImageOptions getImageOptions(boolean cache, boolean allowed) {
        DisplayImageOptions.Builder options = new DisplayImageOptions.Builder();
        options.delayBeforeLoading(10)
                .resetViewBeforeLoading(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(650))
                .cacheOnDisk(cache && allowed)
                .cacheInMemory(!cache || !allowed);
        return options.build();
    }

    public static DisplayImageOptions getIconOptions() {
        DisplayImageOptions.Builder options = new DisplayImageOptions.Builder();
        options.delayBeforeLoading(10)
                .resetViewBeforeLoading(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(650))
                .cacheOnDisk(false)
                .cacheInMemory(false);
        return options.build();
    }

    public static DisplayImageOptions getWallpaperOptions() {
        DisplayImageOptions.Builder options = new DisplayImageOptions.Builder();
        options.delayBeforeLoading(10)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .cacheOnDisk(false)
                .cacheInMemory(false);
        return options.build();
    }

    public static DisplayImageOptions getWallpaperPreviewOptions(boolean isCacheAllowed) {
        DisplayImageOptions.Builder options = new DisplayImageOptions.Builder();
        options.delayBeforeLoading(10)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(isCacheAllowed)
                .cacheInMemory(false);
        return options.build();
    }

}

