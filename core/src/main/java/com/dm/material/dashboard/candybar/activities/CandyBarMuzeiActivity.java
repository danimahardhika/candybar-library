package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Toast;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.callbacks.MuzeiCallback;
import com.dm.material.dashboard.candybar.helpers.LocaleHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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

public abstract class CandyBarMuzeiActivity extends AppCompatActivity implements View.OnClickListener, MuzeiCallback {

    private RadioButton mMinute;
    private RadioButton mHour;
    private NumberPicker mNumberPicker;
    private AppCompatCheckBox mWifiOnly;

    private Class<?> mMuzeiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.setTheme(Preferences.get(this).isDarkTheme() ?
                R.style.AppThemeDark : R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muzei);
        ColorHelper.setupStatusBarIconColor(this);
        ColorHelper.setStatusBarColor(this, ColorHelper.getAttributeColor(this, R.attr.colorPrimaryDark));
        ColorHelper.setNavigationBarColor(this, ContextCompat.getColor(this,
                Preferences.get(this).isDarkTheme() ?
                        R.color.navigationBarDark : R.color.navigationBar));

        mMinute = findViewById(R.id.minute);
        mHour = findViewById(R.id.hour);
        mNumberPicker = findViewById(R.id.number_picker);
        mWifiOnly = findViewById(R.id.wifi_only);
        Toolbar mToolbar = findViewById(R.id.toolbar);

        mMuzeiService = onInit();

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        mNumberPicker.setMinValue(1);
        mNumberPicker.setMaxValue(100);
        setDividerColor(mNumberPicker);
        mMinute.setOnClickListener(this);
        mHour.setOnClickListener(this);

        mWifiOnly.setChecked(Preferences.get(this).isWifiOnly());
        mMinute.setChecked(Preferences.get(this).isRotateMinute());
        mHour.setChecked(!mMinute.isChecked());

        int rotateTime = convertMilliToMinute(Preferences.get(this).getRotateTime());
        if (!mMinute.isChecked()) rotateTime = rotateTime / 60;
        mNumberPicker.setValue(rotateTime);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.setLocale(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_muzei, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_save) {
            if (WallpaperHelper.getWallpaperType(this) != WallpaperHelper.CLOUD_WALLPAPERS) {
                Toast.makeText(this, R.string.muzei_settings_ignored,
                        Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }

            int rotateTime = convertMinuteToMilli(mNumberPicker.getValue());
            if (!mMinute.isChecked()) rotateTime = rotateTime * 60;

            Preferences.get(this).setRotateMinute(mMinute.isChecked());
            Preferences.get(this).setRotateTime(rotateTime);
            Preferences.get(this).setWifiOnly(mWifiOnly.isChecked());

            if (mMuzeiService == null) {
                LogUtil.e("MuzeiService cannot be null");
                return true;
            }

            Intent intent = new Intent(this, mMuzeiService);
            intent.putExtra("restart", true);
            startService(intent);

            Toast.makeText(this, R.string.muzei_settings_saved,
                    Toast.LENGTH_SHORT).show();
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.minute) {
            mMinute.setChecked(true);
            mHour.setChecked(!mMinute.isChecked());
        } else if (id == R.id.hour) {
            mHour.setChecked(true);
            mMinute.setChecked(!mHour.isChecked());
        } else if (id == R.id.wifi_only) {
            mWifiOnly.setChecked(mWifiOnly.isChecked());
        }
    }

    private int convertMinuteToMilli(int minute) {
        return minute * 60 * 1000;
    }

    private int convertMilliToMinute(int milli) {
        return milli / 60 / 1000;
    }

    private void setDividerColor (NumberPicker picker) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, ContextCompat.getDrawable(this,
                            Preferences.get(this).isDarkTheme() ?
                                    R.drawable.numberpicker_divider_dark :
                                    R.drawable.numberpicker_divider));
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
                break;
            }
        }
    }
    
}
