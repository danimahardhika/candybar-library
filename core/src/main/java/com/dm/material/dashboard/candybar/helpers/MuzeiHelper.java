package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.utils.JsonStructure;
import com.dm.material.dashboard.candybar.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

public class MuzeiHelper {

    private final Context mContext;
    private final String mDirectory;

    public MuzeiHelper(@NonNull Context context, String directory) {
        mContext = context;
        mDirectory = directory;
    }

    @Nullable
    public Wallpaper getRandomWallpaper(String wallpaperUrl) throws Exception {
        if (Database.get(mContext).getWallpapersCount() == 0) {
            URL url = new URL(wallpaperUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = new BufferedInputStream(connection.getInputStream());
                List list = JsonHelper.parseList(stream);
                if (list == null) {
                    JsonStructure jsonStructure = CandyBarApplication.getConfiguration().getWallpaperJsonStructure();
                    LogUtil.e("Muzei: Json error: wallpaper array with name "
                            +jsonStructure.getArrayName() +" not found");
                    return null;
                }

                if (list.size() > 0) {
                    int position = getRandomInt(list.size());
                    Wallpaper wallpaper = JsonHelper.getWallpaper(list.get(position));
                    if (wallpaper != null) {
                        if (wallpaper.getName() == null) {
                            wallpaper.setName("Wallpaper");
                        }
                    }
                    return wallpaper;
                }
            }
            return null;
        } else {
            return Database.get(mContext).getRandomWallpaper();
        }
    }

    @Nullable
    public Wallpaper getRandomDownloadedWallpaper() throws Exception {
        List<Wallpaper> downloaded = new ArrayList<>();
        List<Wallpaper> wallpapers = Database.get(mContext).getWallpapers();
        for (Wallpaper wallpaper : wallpapers) {
            File file = new File(mDirectory + File.separator + wallpaper.getName() +
                    WallpaperHelper.IMAGE_EXTENSION);
            if (file.exists()) {
                downloaded.add(wallpaper);
            }
        }

        wallpapers.clear();
        int size = downloaded.size();
        if (size > 0) {
            int position = getRandomInt(size);
            return Wallpaper.Builder()
                    .name(downloaded.get(position).getName())
                    .author(downloaded.get(position).getAuthor())
                    .url(downloaded.get(position).getURL())
                    .thumbUrl(downloaded.get(position).getThumbUrl())
                    .build();
        }
        return null;
    }

    private int getRandomInt(int size) {
        try {
            Random random = new Random();
            return random.nextInt(size);
        } catch (Exception e) {
            return 0;
        }
    }
}
