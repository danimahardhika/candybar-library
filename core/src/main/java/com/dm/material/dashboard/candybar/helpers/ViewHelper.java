package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.items.Home;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

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

public class ViewHelper {

    public static void resetViewBottomMargin(@Nullable View view) {
        if (view == null) return;

        Context context = ContextHelper.getBaseContext(view);
        int orientation = context.getResources().getConfiguration().orientation;

        if (!(view.getLayoutParams() instanceof CoordinatorLayout.LayoutParams))
            return;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        int left = params.leftMargin;
        int right = params.rightMargin;
        int bottom = params.bottomMargin;
        int top = params.topMargin;
        int bottomNavBar = 0;
        int rightNavBar = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean tabletMode = context.getResources().getBoolean(R.bool.tablet_mode);
            if (tabletMode || orientation == Configuration.ORIENTATION_PORTRAIT) {
                bottomNavBar = WindowHelper.getNavigationBarHeight(context);
            } else {
                rightNavBar = WindowHelper.getNavigationBarHeight(context);
            }
        }

        int navBar = WindowHelper.getNavigationBarHeight(context);
        if ((bottom > bottomNavBar) && ((bottom - navBar) > 0))
            bottom -= navBar;
        if ((right > rightNavBar) && ((right - navBar) > 0))
            right -= navBar;

        params.setMargins(left, top, (right + rightNavBar), (bottom + bottomNavBar));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.setMarginEnd((right + rightNavBar));
        }
        view.setLayoutParams(params);
    }

    public static void setFastScrollColor(@Nullable RecyclerFastScroller fastScroll) {
        if (fastScroll == null) return;

        Context context = fastScroll.getContext();
        if (context instanceof ContextThemeWrapper) {
            context = ((ContextThemeWrapper) context).getBaseContext();
        }

        int accent = ColorHelper.getAttributeColor(context, R.attr.colorAccent);

        fastScroll.setBarColor(ColorHelper.setColorAlpha(accent, 0.8f));
        fastScroll.setHandleNormalColor(accent);
        fastScroll.setHandlePressedColor(ColorHelper.getDarkerColor(accent, 0.7f));
    }

    public static Point getWallpaperViewRatio(String viewStyle) {
        switch (viewStyle.toLowerCase(Locale.getDefault())) {
            case "square":
                return new Point(1, 1);
            case "landscape":
                return new Point(16, 9);
            case "portrait":
                return new Point(4, 5);
            default:
                return new Point(1, 1);
        }
    }

    public static Home.Style getHomeImageViewStyle(String viewStyle) {
        switch (viewStyle.toLowerCase(Locale.getDefault())) {
            case "card_square":
                return new Home.Style(new Point(1, 1), Home.Style.Type.CARD_SQUARE);
            case "card_landscape":
                return new Home.Style(new Point(16, 9), Home.Style.Type.CARD_LANDSCAPE);
            case "square":
                return new Home.Style(new Point(1, 1), Home.Style.Type.SQUARE);
            case "landscape":
                return new Home.Style(new Point(16, 9), Home.Style.Type.LANDSCAPE);
            default:
                return new Home.Style(new Point(16, 9), Home.Style.Type.CARD_LANDSCAPE);
        }
    }
}
