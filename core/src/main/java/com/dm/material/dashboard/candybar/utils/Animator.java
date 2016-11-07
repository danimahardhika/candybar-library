package com.dm.material.dashboard.candybar.utils;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AlphaAnimation;

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

public class Animator {

    public static void startAlphaAnimation (@NonNull View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

}
