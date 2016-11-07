package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
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

public class CandyBarSplashActivity extends AppCompatActivity {

    private Handler mHandler;
    private Runnable mRunnable;

    public void initSplashActivity(Bundle savedInstanceState, Class<?> mainActivity, int duration) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        int color = ColorHelper.getDarkerColor(
                ContextCompat.getColor(this, R.color.splashColor), 0.8f);
        ColorHelper.setNavigationBarColor(this, color);
        mRunnable = () -> {
            startActivity(new Intent(this, mainActivity));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, duration);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        if (mHandler != null && mRunnable != null)
            mHandler.removeCallbacks(mRunnable);
        super.onBackPressed();
    }

}

