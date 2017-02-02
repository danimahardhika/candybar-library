package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.preferences.Preferences;

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

public class WallpaperSettingsFragment extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "candybar.dialog.wallpaper.settings";

    private static WallpaperSettingsFragment newInstance() {
        return new WallpaperSettingsFragment();
    }

    public static void showWallpaperSettings(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        try {
            DialogFragment dialog = WallpaperSettingsFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {}
    }

    private AppCompatRadioButton mEnableScrollRadio;
    private AppCompatRadioButton mDisableScrollRadio;
    private LinearLayout mEnableScroll;
    private LinearLayout mDisableScroll;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.title(R.string.menu_wallpaper_settings);
        builder.customView(R.layout.fragment_wallpaper_settings, false);
        builder.positiveText(R.string.close);
        MaterialDialog dialog = builder.build();
        dialog.show();

        mEnableScrollRadio = (AppCompatRadioButton) dialog.findViewById(R.id.enable_scroll_radio);
        mDisableScrollRadio = (AppCompatRadioButton) dialog.findViewById(R.id.disable_scroll_radio);
        mEnableScroll = (LinearLayout) dialog.findViewById(R.id.enable_scroll);
        mDisableScroll = (LinearLayout) dialog.findViewById(R.id.disable_scroll);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEnableScroll.setOnClickListener(this);
        mEnableScroll.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.item_grid_dark : R.drawable.item_grid);

        mDisableScroll.setOnClickListener(this);
        mDisableScroll.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.item_grid_dark : R.drawable.item_grid);

        toggleRadio();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (Preferences.getPreferences(getActivity()).isScrollWallpaper()) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Preferences.getPreferences(getActivity()).setScrollWallpaper(id == R.id.enable_scroll);
        toggleRadio();
    }

    private void toggleRadio() {
        boolean scroll = Preferences.getPreferences(getActivity()).isScrollWallpaper();
        mEnableScrollRadio.setChecked(scroll);
        mDisableScrollRadio.setChecked(!scroll);
    }

}
