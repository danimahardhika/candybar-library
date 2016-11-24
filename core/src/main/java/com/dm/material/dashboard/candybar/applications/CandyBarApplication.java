package com.dm.material.dashboard.candybar.applications;

import android.app.Application;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.nostra13.universalimageloader.core.ImageLoader;

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

    public void initApplication() {
        super.onCreate();
        if (!ImageLoader.getInstance().isInited())
            ImageLoader.getInstance().init(ImageConfig.getImageLoaderConfiguration(this));

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Font-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

}
