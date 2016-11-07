package com.dm.material.dashboard.candybar.items;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

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

@JsonObject
public class WallpaperJSON {

    @JsonField(name = "name")
    public String name;

    @JsonField(name = "author")
    public String author;

    @JsonField(name = "url")
    public String url;

    @JsonField(name = "thumbUrl")
    public String thumbUrl;

    @JsonField(name = "Wallpapers")
    public List<WallpaperJSON> getWalls;

}
