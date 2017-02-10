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

public class Wallpaper {

    private final String mUrl;
    private String mThumbUrl;
    private String mAuthor;
    private String mName;

    public Wallpaper(String name, String author, String url, String thumbUrl) {
        mName = name;
        mAuthor = author;
        mUrl = url;
        mThumbUrl = thumbUrl;
    }

    public String getName() {
        return mName;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public String getURL() {
        return mUrl;
    }


    @Override
    public boolean equals(Object object) {
        boolean equals = false;
        if (object != null && object instanceof Wallpaper) {
            equals = mUrl.equals(((Wallpaper) object).getURL());
        }
        return equals;
    }
}
