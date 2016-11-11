package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Toast;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.Tag;

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

public class CandyBarMuzeiActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioButton mMinute;
    private RadioButton mHour;
    private NumberPicker mNumberPicker;
    private AppCompatCheckBox mDownloadedOnly;
    private AppCompatCheckBox mWifiOnly;

    private Class<?> mMuzeiService;
    
    public void initMuzeiActivity(Bundle savedInstanceState, Class<?> muzeiService) {
        super.setTheme(Preferences.getPreferences(this).isDarkTheme() ?
                R.style.AppThemeDark : R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muzei);
        getWindow().getDecorView().setBackgroundColor(
                ColorHelper.getAttributeColor(this, R.attr.main_background));
        ColorHelper.setStatusBarColor(this, ColorHelper.getAttributeColor(this, R.attr.colorPrimaryDark));
        ViewHelper.resetNavigationBarTranslucent(this, getResources().getConfiguration().orientation);

        mMinute = (RadioButton) findViewById(R.id.minute);
        mHour = (RadioButton) findViewById(R.id.hour);
        mNumberPicker = (NumberPicker) findViewById(R.id.number_picker);
        mDownloadedOnly = (AppCompatCheckBox) findViewById(R.id.downloaded_only);
        mWifiOnly = (AppCompatCheckBox) findViewById(R.id.wifi_only);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mMuzeiService = muzeiService;

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        mNumberPicker.setMinValue(1);
        mNumberPicker.setMaxValue(100);
        setDividerColor(mNumberPicker);

        mMinute.setOnClickListener(this);
        mHour.setOnClickListener(this);

        mWifiOnly.setChecked(Preferences.getPreferences(this).isWifiOnly());
        mDownloadedOnly.setChecked(Preferences.getPreferences(this).isDownloadedOnly());
        mMinute.setChecked(Preferences.getPreferences(this).isRotateMinute());
        mHour.setChecked(!mMinute.isChecked());

        int rotateTime = convertMilliToMinute(Preferences.getPreferences(this).getRotateTime());
        if (!mMinute.isChecked()) rotateTime = rotateTime / 60;
        mNumberPicker.setValue(rotateTime);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarTranslucent(this, newConfig.orientation);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_muzei, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_save) {
            String url = getResources().getString(R.string.wallpaper_json);
            if (!URLUtil.isValidUrl(url)) {
                Toast.makeText(this, R.string.muzei_settings_ignored,
                        Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }

            int rotateTime = convertMinuteToMilli(mNumberPicker.getValue());
            if (!mMinute.isChecked()) rotateTime = rotateTime * 60;

            Preferences.getPreferences(this).setRotateMinute(mMinute.isChecked());
            Preferences.getPreferences(this).setRotateTime(rotateTime);
            Preferences.getPreferences(this).setWifiOnly(mWifiOnly.isChecked());
            Preferences.getPreferences(this).setDownloadedOnly(mDownloadedOnly.isChecked());

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
        } else if (id == R.id.downloaded_only) {
            mDownloadedOnly.setChecked(mDownloadedOnly.isChecked());
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
                            Preferences.getPreferences(this).isDarkTheme() ?
                                    R.drawable.numberpicker_divider_dark :
                                    R.drawable.numberpicker_divider));
                } catch (Exception e) {
                    Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                }
                break;
            }
        }
    }
    
}
