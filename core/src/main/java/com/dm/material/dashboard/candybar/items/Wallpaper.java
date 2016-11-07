package com.dm.material.dashboard.candybar.items;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
 *
 * Licensed under the Apache LicenseHelper, Version 2.0 (the "LicenseHelper");
 * you may not use this file except in compliance with the LicenseHelper.
 * You may obtain a copy of the LicenseHelper at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the LicenseHelper is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the LicenseHelper for the specific language governing permissions and
 * limitations under the LicenseHelper.
 */

public class Wallpaper {

    private String mUrl;
    private String mDate;
    private String mThumbUrl;
    private String mAuthor;
    private String mName;

    public Wallpaper(String name, String author, String url, String thumbUrl) {
        mName = name;
        mAuthor = author;
        mUrl = url;
        mThumbUrl = thumbUrl;
    }

    public Wallpaper(String url, String date) {
        mUrl = url;
        mDate = date;
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

    public String getDate() {
        return mDate;
    }

}
