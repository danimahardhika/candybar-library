package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.HomeAdapter;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;

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

public class TapIntroHelper {

    public static void showHomeIntros(@NonNull Context context, @Nullable RecyclerView recyclerView,
                                      @Nullable StaggeredGridLayoutManager manager, int position) {
        if (Preferences.getPreferences(context).isTimeToShowHomeIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

            new Handler().postDelayed(() -> {
                int primary = ColorHelper.getAttributeColor(context, R.attr.toolbar_icon);
                int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                TapTargetSequence tapTargetSequence = new TapTargetSequence(activity);
                tapTargetSequence.continueOnCancel(true);

                Typeface title = Typeface.createFromAsset(context.getAssets(), "fonts/Font-Medium.ttf");
                Typeface description = Typeface.createFromAsset(context.getAssets(), "fonts/Font-Regular.ttf");

                if (toolbar != null) {
                    tapTargetSequence.target(TapTarget.forToolbarNavigationIcon(toolbar,
                            context.getResources().getString(R.string.tap_intro_home_navigation),
                            context.getResources().getString(R.string.tap_intro_home_navigation_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                            .titleTypeface(title)
                            .descriptionTypeface(description));
                }

                if (recyclerView != null) {
                    HomeAdapter adapter = (HomeAdapter) recyclerView.getAdapter();
                    if (adapter != null) {
                        if (context.getResources().getBoolean(R.bool.enable_apply)) {
                            if (position >= 0 && position < adapter.getItemCount()) {
                                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                                if (holder != null) {
                                    View view = holder.itemView;
                                    if (view != null) {
                                        float targetRadius = ViewHelper.intToDp(context, view.getMeasuredWidth()) - 20f;

                                        String desc = String.format(context.getResources().getString(R.string.tap_intro_home_apply_desc),
                                                context.getResources().getString(R.string.app_name));
                                        tapTargetSequence.target(TapTarget.forView(view,
                                                context.getResources().getString(R.string.tap_intro_home_apply),
                                                desc)
                                                .titleTextColorInt(primary)
                                                .descriptionTextColorInt(secondary)
                                                .targetCircleColorInt(primary)
                                                .targetRadius((int) targetRadius)
                                                .tintTarget(false)
                                                .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                                                .titleTypeface(title)
                                                .descriptionTypeface(description));
                                    }
                                }
                            }
                        }
                    }
                }

                tapTargetSequence.listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        Preferences.getPreferences(context).setTimeToShowHomeIntro(false);
                    }

                    @Override
                    public void onSequenceStep(TapTarget tapTarget, boolean b) {
                        if (manager != null) {
                            if (position >= 0)
                                manager.scrollToPosition(position);
                        }
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget tapTarget) {

                    }
                });
                tapTargetSequence.start();
            }, 100);
        }
    }

    public static void showIconsIntro(@NonNull Context context) {
        if (Preferences.getPreferences(context).isTimeToShowIconsIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
            if (toolbar == null) return;

            new Handler().postDelayed(() -> {
                int primary = ColorHelper.getAttributeColor(context, R.attr.toolbar_icon);
                int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                Typeface title = Typeface.createFromAsset(context.getAssets(), "fonts/Font-Medium.ttf");
                Typeface description = Typeface.createFromAsset(context.getAssets(), "fonts/Font-Regular.ttf");

                TapTargetView.showFor(activity, TapTarget.forToolbarMenuItem(toolbar, R.id.menu_search,
                        context.getResources().getString(R.string.tap_intro_icons_search),
                        context.getResources().getString(R.string.tap_intro_icons_search_desc))
                        .titleTextColorInt(primary)
                        .descriptionTextColorInt(secondary)
                        .targetCircleColorInt(primary)
                        .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                        .titleTypeface(title)
                        .descriptionTypeface(description),
                        new TapTargetView.Listener() {

                            @Override
                            public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                                super.onTargetDismissed(view, userInitiated);
                                Preferences.getPreferences(context).setTimeToShowIconsIntro(false);
                            }
                        });
            }, 100);
        }
    }

    public static void showRequestIntro(@NonNull Context context, @Nullable RecyclerView recyclerView) {
        if (Preferences.getPreferences(context).isTimeToShowRequestIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            int requestOrientation = context.getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_PORTRAIT ?
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            activity.setRequestedOrientation(requestOrientation);

            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

            new Handler().postDelayed(() -> {
                int primary = ColorHelper.getAttributeColor(context, R.attr.toolbar_icon);
                int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                TapTargetSequence tapTargetSequence = new TapTargetSequence(activity);
                tapTargetSequence.continueOnCancel(true);

                Typeface title = Typeface.createFromAsset(context.getAssets(), "fonts/Font-Medium.ttf");
                Typeface description = Typeface.createFromAsset(context.getAssets(), "fonts/Font-Regular.ttf");

                if (recyclerView != null) {
                    int position = 0;
                    if (Preferences.getPreferences(context).isPremiumRequestEnabled())
                        position += 1;

                    if (recyclerView.getAdapter() != null) {
                        if (position < recyclerView.getAdapter().getItemCount()) {
                            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);

                            if (holder != null) {
                                View view = holder.itemView.findViewById(R.id.checkbox);
                                if (view != null) {
                                    tapTargetSequence.target(TapTarget.forView(view,
                                            context.getResources().getString(R.string.tap_intro_request_select),
                                            context.getResources().getString(R.string.tap_intro_request_select_desc))
                                            .titleTextColorInt(primary)
                                            .descriptionTextColorInt(secondary)
                                            .targetCircleColorInt(primary)
                                            .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                                            .titleTypeface(title)
                                            .descriptionTypeface(description));
                                }
                            }
                        }
                    }
                }

                if (toolbar != null) {
                    tapTargetSequence.target(TapTarget.forToolbarMenuItem(toolbar, R.id.menu_select_all,
                            context.getResources().getString(R.string.tap_intro_request_select_all),
                            context.getResources().getString(R.string.tap_intro_request_select_all_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                            .titleTypeface(title)
                            .descriptionTypeface(description));
                }

                View fab = activity.findViewById(R.id.fab);
                if (fab != null) {
                    tapTargetSequence.target(TapTarget.forView(activity.findViewById(R.id.fab),
                            context.getResources().getString(R.string.tap_intro_request_send),
                            context.getResources().getString(R.string.tap_intro_request_send_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .tintTarget(false)
                            .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                            .titleTypeface(title)
                            .descriptionTypeface(description));
                }

                if (Preferences.getPreferences(context).isPremiumRequestEnabled()) {
                    if (!Preferences.getPreferences(context).isPremiumRequest()) {
                        if (recyclerView != null) {
                            int position = 0;

                            if (recyclerView.getAdapter() != null) {
                                if (position < recyclerView.getAdapter().getItemCount()) {
                                    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);

                                    if (holder != null) {
                                        View view = holder.itemView.findViewById(R.id.buy);
                                        if (view != null) {
                                            float targetRadius = ViewHelper.intToDp(context, view.getMeasuredWidth()) - 10f;

                                            tapTargetSequence.target(TapTarget.forView(view,
                                                    context.getResources().getString(R.string.tap_intro_request_premium),
                                                    context.getResources().getString(R.string.tap_intro_request_premium_desc))
                                                    .titleTextColorInt(primary)
                                                    .descriptionTextColorInt(secondary)
                                                    .targetCircleColorInt(primary)
                                                    .targetRadius((int) targetRadius)
                                                    .tintTarget(false)
                                                    .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                                                    .titleTypeface(title)
                                                    .descriptionTypeface(description));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                tapTargetSequence.listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        Preferences.getPreferences(context).setTimeToShowRequestIntro(false);
                    }

                    @Override
                    public void onSequenceStep(TapTarget tapTarget, boolean b) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget tapTarget) {

                    }
                });
                tapTargetSequence.start();
            }, 100);
        }
    }

    public static void showWallpapersIntro(@NonNull Context context, @Nullable RecyclerView recyclerView) {
        if (Preferences.getPreferences(context).isTimeToShowWallpapersIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            new Handler().postDelayed(() -> {
                int primary = ColorHelper.getAttributeColor(context, R.attr.toolbar_icon);
                int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                if (recyclerView != null) {
                    TapTargetSequence tapTargetSequence = new TapTargetSequence(activity);
                    tapTargetSequence.continueOnCancel(true);

                    int position = 0;

                    if (recyclerView.getAdapter() == null)
                        return;

                    if (position < recyclerView.getAdapter().getItemCount()) {
                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                        if (holder == null) return;

                        View view = holder.itemView.findViewById(R.id.image);
                        if (view != null) {
                            float targetRadius = ViewHelper.intToDp(context, view.getMeasuredWidth()) - 10f;

                            Typeface title = Typeface.createFromAsset(context.getAssets(), "fonts/Font-Medium.ttf");
                            Typeface description = Typeface.createFromAsset(context.getAssets(), "fonts/Font-Regular.ttf");

                            String desc = String.format(context.getResources().getString(R.string.tap_intro_wallpapers_option_desc),
                                    context.getResources().getBoolean(R.bool.enable_wallpaper_download) ?
                                    context.getResources().getString(R.string.tap_intro_wallpapers_option_desc_download) : "");
                            tapTargetSequence.target(TapTarget.forView(view,
                                    context.getResources().getString(R.string.tap_intro_wallpapers_option),
                                    desc)
                                    .titleTextColorInt(primary)
                                    .descriptionTextColorInt(secondary)
                                    .targetCircleColorInt(primary)
                                    .targetRadius((int) targetRadius)
                                    .tintTarget(false)
                                    .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                                    .titleTypeface(title)
                                    .descriptionTypeface(description));

                            tapTargetSequence.target(TapTarget.forView(view,
                                    context.getResources().getString(R.string.tap_intro_wallpapers_preview),
                                    context.getResources().getString(R.string.tap_intro_wallpapers_preview_desc))
                                    .titleTextColorInt(primary)
                                    .descriptionTextColorInt(secondary)
                                    .targetCircleColorInt(primary)
                                    .targetRadius((int) targetRadius)
                                    .tintTarget(false)
                                    .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                                    .titleTypeface(title)
                                    .descriptionTypeface(description));

                            tapTargetSequence.listener(new TapTargetSequence.Listener() {
                                @Override
                                public void onSequenceFinish() {
                                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                    Preferences.getPreferences(context).setTimeToShowWallpapersIntro(false);
                                }

                                @Override
                                public void onSequenceStep(TapTarget tapTarget, boolean b) {

                                }

                                @Override
                                public void onSequenceCanceled(TapTarget tapTarget) {

                                }
                            });
                            tapTargetSequence.start();
                        }
                    }
                }
            }, 200);
        }
    }

    public static void showWallpaperPreviewIntro(@NonNull Context context, @ColorInt int color) {
        if (Preferences.getPreferences(context).isTimeToShowWallpaperPreviewIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

            new Handler().postDelayed(() -> {
                int primary = ColorHelper.getTitleTextColor(color);
                int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                TapTargetSequence tapTargetSequence = new TapTargetSequence(activity);
                tapTargetSequence.continueOnCancel(true);

                Typeface title = Typeface.createFromAsset(context.getAssets(), "fonts/Font-Medium.ttf");
                Typeface description = Typeface.createFromAsset(context.getAssets(), "fonts/Font-Regular.ttf");

                if (toolbar != null) {
                    tapTargetSequence.target(TapTarget.forToolbarMenuItem(toolbar, R.id.menu_wallpaper_settings,
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_settings),
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_settings_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .outerCircleColorInt(color)
                            .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                            .titleTypeface(title)
                            .descriptionTypeface(description));

                    if (context.getResources().getBoolean(R.bool.enable_wallpaper_download)) {
                        tapTargetSequence.target(TapTarget.forToolbarMenuItem(toolbar, R.id.menu_save,
                                context.getResources().getString(R.string.tap_intro_wallpaper_preview_save),
                                context.getResources().getString(R.string.tap_intro_wallpaper_preview_save_desc))
                                .titleTextColorInt(primary)
                                .descriptionTextColorInt(secondary)
                                .targetCircleColorInt(primary)
                                .outerCircleColorInt(color)
                                .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                                .titleTypeface(title)
                                .descriptionTypeface(description));
                    }
                }

                View fab = activity.findViewById(R.id.fab);
                if (fab != null) {
                    tapTargetSequence.target(TapTarget.forView(fab,
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_apply),
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_apply_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .outerCircleColorInt(color)
                            .tintTarget(false)
                            .drawShadow(Preferences.getPreferences(context).isShadowEnabled())
                            .titleTypeface(title)
                            .descriptionTypeface(description));
                }

                tapTargetSequence.listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        Preferences.getPreferences(context).setTimeToShowWallpaperPreviewIntro(false);
                    }

                    @Override
                    public void onSequenceStep(TapTarget tapTarget, boolean b) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget tapTarget) {

                    }
                });
                tapTargetSequence.start();
            }, 100);
        }
    }
}
