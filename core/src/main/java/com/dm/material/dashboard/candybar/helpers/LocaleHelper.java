package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dm.material.dashboard.candybar.utils.Tag;

import java.util.Locale;

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

public class LocaleHelper {

    @Nullable
    public static String getOtherAppLocaleName(Context context, Locale locale, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            Resources res = packageManager.getResourcesForApplication(packageName);
            Context otherAppContext = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
            Configuration configuration = new Configuration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration = res.getConfiguration();
                configuration.setLocale(locale);
                return otherAppContext.createConfigurationContext(configuration).getString(info.labelRes);
            }

            configuration.locale = locale;
            res.updateConfiguration(configuration, context.getResources().getDisplayMetrics());
            return res.getString(info.labelRes);
        } catch (Exception e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
        return null;
    }

}
