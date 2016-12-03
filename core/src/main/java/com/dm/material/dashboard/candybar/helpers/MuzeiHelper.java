package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.items.WallpaperJSON;

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

    private final Database mDatabase;
    private final String mDirectory;

    public MuzeiHelper(@NonNull Context context, String directory) {
        mDatabase = new Database(context);
        mDirectory = directory;
    }

    public Wallpaper getRandomWallpaper(String wallpaperUrl) throws Exception {
        if (mDatabase.isWallpapersEmpty()) {
            URL url = new URL(wallpaperUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = new BufferedInputStream(connection.getInputStream());
                WallpaperJSON wallpapers = LoganSquare.parse(stream, WallpaperJSON.class);
                int size = wallpapers.getWalls.size();
                if (size > 0) {
                    int position = getRandomInt(size);
                    return new Wallpaper(
                            wallpapers.getWalls.get(position).name,
                            wallpapers.getWalls.get(position).author,
                            wallpapers.getWalls.get(position).url,
                            wallpapers.getWalls.get(position).thumbUrl);
                }
            }
            return null;
        } else {
            return mDatabase.getRandomWallpaper();
        }
    }

    @Nullable
    public Wallpaper getRandomDownloadedWallpaper() throws Exception {
        List<Wallpaper> downloaded = new ArrayList<>();
        List<Wallpaper> wallpapers = mDatabase.getWallpapers();
        for (Wallpaper wallpaper : wallpapers) {
            File file = new File(mDirectory + File.separator + wallpaper.getName() +
                    FileHelper.IMAGE_EXTENSION);
            if (file.exists()) {
                downloaded.add(wallpaper);
            }
        }

        wallpapers.clear();
        int size = downloaded.size();
        if (size > 0) {
            int position = getRandomInt(size);
            return new Wallpaper(
                    downloaded.get(position).getName(),
                    downloaded.get(position).getAuthor(),
                    downloaded.get(position).getURL(),
                    downloaded.get(position).getThumbUrl());
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
