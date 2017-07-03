package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.JsonStructure;
import com.dm.material.dashboard.candybar.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

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

public class JsonHelper {

    @Nullable
    public static List parseList(@NonNull InputStream stream) {
        List list = null;
        JsonStructure jsonStructure = CandyBarApplication.getConfiguration().getWallpaperJsonStructure();

        try {
            if (jsonStructure.arrayName() == null) {
                list = LoganSquare.parseList(stream, Map.class);
            } else {
                Map<String, List> map = LoganSquare.parseMap(stream, List.class);
                list = map.get(jsonStructure.arrayName());
            }
        } catch (IOException e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return list;
    }

    @Nullable
    public static Wallpaper getWallpaper(@NonNull Object object) {
        if (object instanceof Map) {
            JsonStructure jsonStructure = CandyBarApplication.getConfiguration().getWallpaperJsonStructure();
            Map map = (Map) object;
            return new Wallpaper(
                    (String) map.get(jsonStructure.name()),
                    (String) map.get(jsonStructure.author()),
                    (String) map.get(jsonStructure.url()),
                    getThumbUrl(map));
        }
        return null;
    }

    public static String getGeneratedName(@NonNull Context context, @Nullable String name) {
        if (name == null) {
            String generatedName = "Wallpaper " +Preferences.get(context).getAutoIncrement();
            Preferences.get(context).setAutoIncrement(
                    Preferences.get(context).getAutoIncrement() + 1);
            return generatedName;
        }
        return name;
    }

    public static String getThumbUrl(@NonNull Map map) {
        JsonStructure jsonStructure = CandyBarApplication.getConfiguration().getWallpaperJsonStructure();
        String url = (String) map.get(jsonStructure.url());
        if (jsonStructure.thumbUrl() == null) return url;

        String thumbUrl = (String) map.get(jsonStructure.thumbUrl());
        if (thumbUrl == null) return url;
        return thumbUrl;
    }
}
