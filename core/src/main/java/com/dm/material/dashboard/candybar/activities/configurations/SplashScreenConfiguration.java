package com.dm.material.dashboard.candybar.activities.configurations;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.dm.material.dashboard.candybar.helpers.TypefaceHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

public class SplashScreenConfiguration {

    @NonNull private Class<?> mMainActivity;
    @ColorInt private int mBottomTextColor;
    private String mBottomText;
    @FontSize private int mBottomTextSize;
    @FontStyle private int mBottomTextFont;

    public SplashScreenConfiguration(@NonNull Class<?> mainActivity) {
        mMainActivity = mainActivity;
        mBottomTextColor = -1;
        mBottomTextFont = FontStyle.REGULAR;
        mBottomTextSize = FontSize.REGULAR;
    }

    public SplashScreenConfiguration setBottomText(String text) {
        mBottomText = text;
        return this;
    }

    public SplashScreenConfiguration setBottomTextColor(@ColorInt int color) {
        mBottomTextColor = color;
        return this;
    }

    public SplashScreenConfiguration setBottomTextSize(@FontSize int fontSize) {
        mBottomTextSize = fontSize;
        return this;
    }

    public SplashScreenConfiguration setBottomTextFont(@FontStyle int fontStyle) {
        mBottomTextFont = fontStyle;
        return this;
    }

    public Class<?> getMainActivity() {
        return mMainActivity;
    }

    public String getBottomText() {
        return mBottomText;
    }

    public int getBottomTextColor() {
        return mBottomTextColor;
    }

    public float getBottomTextSize() {
        switch (mBottomTextSize) {
            case FontSize.SMALL:
                return 14f;
            case FontSize.LARGE:
                return 16f;
            case FontSize.REGULAR:
            default:
                return 15f;
        }
    }

    public Typeface getBottomTextFont(@NonNull Context context) {
        switch (mBottomTextFont) {
            case FontStyle.MEDIUM:
                return TypefaceHelper.getMedium(context);
            case FontStyle.BOLD:
                return TypefaceHelper.getBold(context);
            case FontStyle.REGULAR:
            default:
                return TypefaceHelper.getRegular(context);
        }
    }

    @IntDef({FontSize.SMALL, FontSize.REGULAR, FontSize.LARGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FontSize {
        int SMALL = 0;
        int REGULAR = 1;
        int LARGE = 2;
    }

    @IntDef({FontStyle.REGULAR, FontStyle.MEDIUM, FontStyle.BOLD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FontStyle {
        int REGULAR = 0;
        int MEDIUM = 1;
        int BOLD = 2;
    }
}
