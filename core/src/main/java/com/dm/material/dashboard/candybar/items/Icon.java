package com.dm.material.dashboard.candybar.items;

import android.support.annotation.NonNull;

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

public class Icon {

    private String mTitle;
    private int mRes;
    private String mPackageName;
    private List<Icon> mIcons;

    public Icon(String title, int res) {
        mTitle = title;
        mRes = res;
    }

    public Icon(String title, int res, String packageName) {
        mTitle = title;
        mRes = res;
        mPackageName = packageName;
    }

    public Icon(String title, @NonNull List<Icon> icons) {
        mTitle = title;
        mIcons = icons;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getRes() {
        return mRes;
    }

    public String getPackageName() {
        return mPackageName;
    }

    @NonNull
    public List<Icon> getIcons() {
        return mIcons;
    }

    public void setIcons(List<Icon> icons) {
        mIcons = icons;
    }

    @Override
    public boolean equals(Object object) {
        boolean res = false;
        boolean title = false;
        if (object != null && object instanceof Icon) {
            res = mRes == ((Icon) object).getRes();
            title = mTitle.equals(((Icon) object).getTitle());
        }
        return res && title;
    }
}
