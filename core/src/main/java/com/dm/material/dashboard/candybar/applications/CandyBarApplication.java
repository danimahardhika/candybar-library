package com.dm.material.dashboard.candybar.applications;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarCrashReport;
import com.dm.material.dashboard.candybar.helpers.LocaleHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.JsonStructure;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

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

public class CandyBarApplication extends Application {

    private static Configuration mConfiguration;
    private Thread.UncaughtExceptionHandler mHandler;

    public static Configuration getConfiguration() {
        if (mConfiguration == null) {
            mConfiguration = new Configuration();
        }
        return mConfiguration;
    }

    public void initApplication() {
        initApplication(new Configuration());
    }

    public void initApplication(@NonNull Configuration configuration) {
        super.onCreate();
        mConfiguration = configuration;
        if (!ImageLoader.getInstance().isInited())
            ImageLoader.getInstance().init(ImageConfig.getImageLoaderConfiguration(this));

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Font-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        //Enable or disable logging
        LogUtil.setLoggingEnabled(true);

        if (mConfiguration.mIsCrashReportEnabled) {
            mHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
        }

        if (Preferences.get(this).isTimeToSetLanguagePreference()) {
            Preferences.get(this).setLanguagePreference();
            return;
        }

        LocaleHelper.setLocale(this);
    }

    private void handleUncaughtException(Thread thread, Throwable throwable) {
        try {
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String dateTime = dateFormat.format(new Date());
            sb.append("Crash Time : ").append(dateTime).append("\n");
            sb.append("Class Name : ").append(throwable.getClass().getName()).append("\n");
            sb.append("Caused By : ").append(throwable.toString()).append("\n");

            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("\n");
            sb.append(element.toString());
        }

            Preferences.get(this).setLatestCrashLog(sb.toString());

            Intent intent = new Intent(this, CandyBarCrashReport.class);
            intent.putExtra(CandyBarCrashReport.EXTRA_STACKTRACE, sb.toString());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        } catch (Exception e) {
            if (mHandler != null) {
                mHandler.uncaughtException(thread, throwable);
                return;
            }
        }
        System.exit(1);
    }

    public static class Configuration {

        private NavigationIcon mNavigationIcon = NavigationIcon.STYLE_1;
        private NavigationViewHeader mNavigationViewHeader = NavigationViewHeader.NORMAL;

        private GridStyle mHomeGrid = GridStyle.CARD;
        private GridStyle mApplyGrid = GridStyle.CARD;
        private Style mRequestStyle = Style.PORTRAIT_FLAT_LANDSCAPE_CARD;
        private GridStyle mWallpapersGrid = GridStyle.CARD;
        private Style mAboutStyle = Style.PORTRAIT_FLAT_LANDSCAPE_CARD;
        private IconColor mIconColor = IconColor.PRIMARY_TEXT;

        private boolean mIsAutomaticIconsCountEnabled = true;
        private int mCustomIconsCount = 0;
        private boolean mIsShowTabIconsCount = false;
        private boolean mIsShowTabAllIcons = false;
        private String mTabAllIconsTitle = "All Icons";
        private String[] mCategoryForTabAllIcons = null;

        private boolean mIsShadowEnabled = true;
        private boolean mIsDashboardThemingEnabled = true;
        private int mWallpaperGridPreviewQuality = 4;

        private boolean mIsGenerateAppFilter = true;
        private boolean mIsGenerateAppMap = false;
        private boolean mIsGenerateThemeResources = false;
        private boolean mIsIncludeIconRequestToEmailBody = true;

        private boolean mIsCrashReportEnabled = true;
        private JsonStructure mWallpaperJsonStructure = new JsonStructure.Builder("Wallpapers").build();

        public Configuration setNavigationIcon(@NonNull NavigationIcon navigationIcon) {
            mNavigationIcon = navigationIcon;
            return this;
        }

        public Configuration setNavigationViewHeaderStyle(@NonNull NavigationViewHeader navigationViewHeader) {
            mNavigationViewHeader = navigationViewHeader;
            return this;
        }

        public Configuration setAutomaticIconsCountEnabled(boolean automaticIconsCountEnabled) {
            mIsAutomaticIconsCountEnabled = automaticIconsCountEnabled;
            return this;
        }

        public Configuration setHomeGridStyle(@NonNull GridStyle gridStyle) {
            mHomeGrid = gridStyle;
            return this;
        }

        public Configuration setApplyGridStyle(@NonNull GridStyle gridStyle) {
            mApplyGrid = gridStyle;
            return this;
        }

        public Configuration setRequestStyle(@NonNull Style style) {
            mRequestStyle = style;
            return this;
        }

        public Configuration setWallpapersGridStyle(@NonNull GridStyle gridStyle) {
            mWallpapersGrid = gridStyle;
            return this;
        }

        public Configuration setAboutStyle(@NonNull Style style) {
            mAboutStyle = style;
            return this;
        }

        public Configuration setSocialIconColor(@NonNull IconColor iconColor) {
            mIconColor = iconColor;
            return this;
        }

        public Configuration setCustomIconsCount(int customIconsCount) {
            mCustomIconsCount = customIconsCount;
            return this;
        }

        public Configuration setShowTabIconsCount(boolean showTabIconsCount) {
            mIsShowTabIconsCount = showTabIconsCount;
            return this;
        }

        public Configuration setShowTabAllIcons(boolean showTabAllIcons) {
            mIsShowTabAllIcons = showTabAllIcons;
            return this;
        }

        public Configuration setTabAllIconsTitle(@NonNull String title) {
            mTabAllIconsTitle = title;
            if (mTabAllIconsTitle.length() == 0) mTabAllIconsTitle = "All Icons";
            return this;
        }

        public Configuration setCategoryForTabAllIcons(@NonNull String[] categories) {
            mCategoryForTabAllIcons = categories;
            return this;
        }

        public Configuration setShadowEnabled(boolean shadowEnabled) {
            mIsShadowEnabled = shadowEnabled;
            return this;
        }

        public Configuration setDashboardThemingEnabled(boolean dashboardThemingEnabled) {
            mIsDashboardThemingEnabled = dashboardThemingEnabled;
            return this;
        }

        public Configuration setWallpaperGridPreviewQuality(@IntRange (from = 1, to = 10) int quality) {
            mWallpaperGridPreviewQuality = quality;
            return this;
        }

        public Configuration setGenerateAppFilter(boolean generateAppFilter) {
            mIsGenerateAppFilter = generateAppFilter;
            return this;
        }

        public Configuration setGenerateAppMap(boolean generateAppMap) {
            mIsGenerateAppMap = generateAppMap;
            return this;
        }

        public Configuration setGenerateThemeResources(boolean generateThemeResources) {
            mIsGenerateThemeResources = generateThemeResources;
            return this;
        }

        public Configuration setIncludeIconRequestToEmailBody(boolean includeIconRequestToEmailBody) {
            mIsIncludeIconRequestToEmailBody = includeIconRequestToEmailBody;
            return this;
        }

        public Configuration setCrashReportEnabled(boolean crashReportEnabled) {
            mIsCrashReportEnabled = crashReportEnabled;
            return this;
        }

        public Configuration setWallpaperJsonStructure(@NonNull JsonStructure jsonStructure) {
            mWallpaperJsonStructure = jsonStructure;
            return this;
        }

        public NavigationIcon getNavigationIcon() {
            return mNavigationIcon;
        }

        public NavigationViewHeader getNavigationViewHeader() {
            return mNavigationViewHeader;
        }

        public GridStyle getHomeGrid() {
            return mHomeGrid;
        }

        public GridStyle getApplyGrid() {
            return mApplyGrid;
        }

        public Style getRequestStyle() {
            return mRequestStyle;
        }

        public GridStyle getWallpapersGrid() {
            return mWallpapersGrid;
        }

        public Style getAboutStyle() {
            return mAboutStyle;
        }

        public IconColor getSocialIconColor() {
            return mIconColor;
        }

        public boolean isAutomaticIconsCountEnabled() {
            return mIsAutomaticIconsCountEnabled;
        }

        public int getCustomIconsCount() {
            return mCustomIconsCount;
        }

        public boolean isShowTabIconsCount() {
            return mIsShowTabIconsCount;
        }

        public boolean isShowTabAllIcons() {
            return mIsShowTabAllIcons;
        }

        public String getTabAllIconsTitle() {
            return mTabAllIconsTitle;
        }

        public String[] getCategoryForTabAllIcons() {
            return mCategoryForTabAllIcons;
        }

        public boolean isShadowEnabled() {
            return mIsShadowEnabled;
        }

        public boolean isDashboardThemingEnabled() {
            return mIsDashboardThemingEnabled;
        }

        public int getWallpaperGridPreviewQuality() {
            return mWallpaperGridPreviewQuality;
        }

        public boolean isGenerateAppFilter() {
            return mIsGenerateAppFilter;
        }

        public boolean isGenerateAppMap() {
            return mIsGenerateAppMap;
        }

        public boolean isGenerateThemeResources() {
            return mIsGenerateThemeResources;
        }

        public boolean isIncludeIconRequestToEmailBody() {
            return mIsIncludeIconRequestToEmailBody;
        }

        public JsonStructure getWallpaperJsonStructure() {
            return mWallpaperJsonStructure;
        }
    }

    public enum NavigationIcon {
        DEFAULT,
        STYLE_1,
        STYLE_2,
        STYLE_3,
        STYLE_4
    }

    public enum NavigationViewHeader {
        NORMAL,
        MINI,
        NONE
    }

    public enum GridStyle {
        CARD,
        FLAT
    }

    public enum Style {
        PORTRAIT_FLAT_LANDSCAPE_CARD,
        PORTRAIT_FLAT_LANDSCAPE_FLAT
    }

    public enum IconColor {
        PRIMARY_TEXT,
        ACCENT
    }
}
