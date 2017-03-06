package com.dm.material.dashboard.candybar.items;

import android.support.annotation.ColorInt;

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

public class Feature {

    private String mTitle;
    private final int mColor;

    public Feature(@ColorInt int color, String title) {
        mColor = color;
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @ColorInt
    public int getColor() {
        return mColor;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;
        if (object != null && object instanceof Feature) {
            equals = mColor == ((Feature) object).getColor() &&
                    mTitle.equals(((Feature) object).getTitle());
        }
        return equals;
    }
}
