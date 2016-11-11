package com.dm.material.dashboard.candybar.services;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.URLUtil;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.dm.material.dashboard.candybar.helpers.FileHelper;
import com.dm.material.dashboard.candybar.helpers.MuzeiHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.preferences.Preferences;

import java.io.File;

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

public abstract class CandyBarMuzeiService extends RemoteMuzeiArtSource {

    private MuzeiHelper mMuzeiHelper;

    public CandyBarMuzeiService(String name) {
        super(name);
    }

    public void startCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            boolean restart = intent.getBooleanExtra("restart", false);
            if (restart) {
                try {
                    onTryUpdate(UPDATE_REASON_USER_NEXT);
                } catch (RetryException ignored) {}
            }
        }
    }

    public void initMuzeiService() {
        super.onCreate();
        mMuzeiHelper = new MuzeiHelper(this,
                WallpaperHelper.getDefaultWallpapersDirectory(this).toString());
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    public void tryUpdate(String wallpaperUrl) {
        if (!URLUtil.isValidUrl(wallpaperUrl))
            return;

        try {
            Wallpaper wallpaper = null;
            if (Preferences.getPreferences(this).isDownloadedOnly())
                wallpaper = mMuzeiHelper.getRandomDownloadedWallpaper();

            if (wallpaper == null) {
                if (Preferences.getPreferences(this).isDownloadedOnly())
                    Preferences.getPreferences(this).setDownloadedOnly(false);
                wallpaper = mMuzeiHelper.getRandomWallpaper(wallpaperUrl);
            }

            if (Preferences.getPreferences(this).isConnectedAsPreferred())
                if (wallpaper != null) publishArtwork(wallpaper);
        } catch (Exception ignored) {}
    }

    private void publishArtwork(Wallpaper wallpaper) {
        File file = new File(Preferences.getPreferences(this).getWallsDirectory()
                + wallpaper.getName() + FileHelper.IMAGE_EXTENSION);
        publishArtwork(new Artwork.Builder()
                .title(wallpaper.getName())
                .byline(wallpaper.getAuthor())
                .imageUri(file.exists() ?
                        Uri.fromFile(file) :
                        Uri.parse(wallpaper.getURL()))
                .build());

        scheduleUpdate(System.currentTimeMillis() +
                Preferences.getPreferences(this).getRotateTime());
    }

}

