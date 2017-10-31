package com.dm.material.dashboard.candybar.services;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.databases.Database;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.dm.material.dashboard.candybar.helpers.MuzeiHelper;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.preferences.Preferences;

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

public abstract class CandyBarMuzeiService extends RemoteMuzeiArtSource {

    public CandyBarMuzeiService(String name) {
        super(name);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent == null || intent.getExtras() == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        Bundle bundle = intent.getExtras();
        boolean restart = bundle.getBoolean("restart", false);
        if (restart) {
            try {
                onTryUpdate(UPDATE_REASON_USER_NEXT);
            } catch (RetryException ignored) {}
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        try {
            if (!URLUtil.isValidUrl(getString(R.string.wallpaper_json)))
                return;

            Wallpaper wallpaper = MuzeiHelper.getRandomWallpaper(this);

            if (Preferences.get(this).isConnectedAsPreferred()) {
                if (wallpaper != null) {
                    Uri uri = Uri.parse(wallpaper.getURL());

                    publishArtwork(new Artwork.Builder()
                            .title(wallpaper.getName())
                            .byline(wallpaper.getAuthor())
                            .imageUri(uri)
                            .build());

                    scheduleUpdate(System.currentTimeMillis() +
                            Preferences.get(this).getRotateTime());
                }
            }

            Database.get(this).closeDatabase();
        } catch (Exception ignored) {}
    }
}

