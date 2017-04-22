package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;

import com.dm.material.dashboard.candybar.R;
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

public class NavigationViewHelper {

    public static void initApply(NavigationView navigationView) {
        Context context = ContextHelper.getBaseContext(navigationView);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.navigation_view_apply);
        if (menuItem == null) return;

        menuItem.setVisible(context.getResources().getBoolean(R.bool.enable_apply));
    }

    public static void initIconRequest(NavigationView navigationView) {
        Context context = ContextHelper.getBaseContext(navigationView);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.navigation_view_request);
        if (menuItem == null) return;

        menuItem.setVisible(context.getResources().getBoolean(R.bool.enable_icon_request) ||
                Preferences.getPreferences(context).isPremiumRequestEnabled());
    }

    public static void initWallpapers(NavigationView navigationView) {
        Context context = ContextHelper.getBaseContext(navigationView);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.navigation_view_wallpapers);
        if (menuItem == null) return;

        if (WallpaperHelper.getWallpaperType(context) == WallpaperHelper.UNKNOWN)
            menuItem.setVisible(false);
    }

    public static void hideScrollBar(NavigationView navigationView) {
        NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        if (navigationMenuView != null)
            navigationMenuView.setVerticalScrollBarEnabled(false);
    }
}
