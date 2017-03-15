package com.dm.material.dashboard.candybar.items;

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

import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

public class Setting {

    private int mIcon;
    private String mTitle;
    private String mSubtitle;
    private String mContent;
    private String mFooter;
    private Setting.Type mType;
    private int mCheckState;

    public Setting(@DrawableRes int icon, String title, String subtitle, String content, String footer,
                   @NonNull Setting.Type type, @IntRange(from = -1, to = 1) int checkState) {
        mIcon = icon;
        mTitle = title;
        mSubtitle = subtitle;
        mContent = content;
        mFooter = footer;
        mType = type;
        mCheckState = checkState;
    }

    @DrawableRes
    public int getIcon() {
        return mIcon;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public String getContent() {
        return mContent;
    }

    public String getFooter() {
        return mFooter;
    }

    public Setting.Type getType() {
        return mType;
    }

    public int getCheckState() {
        return mCheckState;
    }

    public void setFooter(String footer) {
        mFooter = footer;
    }

   public enum Type {
       HEADER(0),
       CACHE(1),
       ICON_REQUEST(2),
       RESTORE(3),
       PREMIUM_REQUEST(4),
       THEME(4),
       WALLPAPER(5),
       REPORT_BUGS(6),
       CHANGELOG(7);

       private int mType;

       Type(int type) {
           mType = type;
       }

       public int getType() {
           return mType;
       }
   }
}
