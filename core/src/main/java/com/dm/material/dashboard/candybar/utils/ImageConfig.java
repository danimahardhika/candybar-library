package com.dm.material.dashboard.candybar.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
 *
 * Licensed under the Apache LicenseHelper, Version 2.0 (the "LicenseHelper");
 * you may not use this file except in compliance with the LicenseHelper.
 * You may obtain a copy of the LicenseHelper at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the LicenseHelper is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the LicenseHelper for the specific language governing permissions and
 * limitations under the LicenseHelper.
 */

public class ImageConfig {

    public static ImageLoaderConfiguration getImageLoaderConfiguration(@NonNull Context context) {
        return new ImageLoaderConfiguration.Builder(context)
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
                .displayer(new FadeInBitmapDisplayer(650));
        if (cache) options.cacheOnDisk(allowed);
        else {
            options.cacheOnDisk(false)
                    .cacheInMemory(true);
        }
        return options.build();
    }

    public static DisplayImageOptions getImageOptions(ImageScaleType scaleType) {
        DisplayImageOptions.Builder options = new DisplayImageOptions.Builder();
        options.delayBeforeLoading(10)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .imageScaleType(scaleType)
                .cacheOnDisk(false)
                .cacheInMemory(true);
        return options.build();
    }

}

