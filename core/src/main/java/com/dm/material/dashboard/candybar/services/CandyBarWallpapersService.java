package com.dm.material.dashboard.candybar.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.JsonHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Wallpaper;
import com.dm.material.dashboard.candybar.receivers.CandyBarBroadcastReceiver;
import com.dm.material.dashboard.candybar.utils.LogUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

public class CandyBarWallpapersService extends IntentService {

    private static final String SERVICE = "candybar.wallpapers.service";

    public CandyBarWallpapersService() {
        super(SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (WallpaperHelper.getWallpaperType(this) != WallpaperHelper.CLOUD_WALLPAPERS)
                return;

            String wallpaperUrl = getResources().getString(R.string.wallpaper_json);
            URL url = new URL(wallpaperUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(CandyBarBroadcastReceiver.PROCESS_RESPONSE);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

            LogUtil.d("Wallpaper service started from: " +getPackageName());

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = connection.getInputStream();
                List list = JsonHelper.parseList(stream);
                if (list == null) return;

                List<Wallpaper> wallpapers = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    Wallpaper wallpaper = JsonHelper.getWallpaper(list.get(i));
                    if (wallpaper != null) {
                        if (!wallpapers.contains(wallpaper)) {
                            wallpapers.add(wallpaper);
                        } else {
                            LogUtil.e("Duplicate wallpaper found: " +wallpaper.getURL());
                        }
                    }
                }

                int size = wallpapers.size();
                broadcastIntent.putExtra("size", size);
                broadcastIntent.putExtra("packageName", getPackageName());
                sendBroadcast(broadcastIntent);
            }
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }
}
