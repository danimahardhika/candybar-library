package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.FileHelper;
import com.dm.material.dashboard.candybar.helpers.PermissionHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;

import java.io.File;

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

public class WallpaperOptionsFragment extends DialogFragment implements View.OnClickListener {

    private LinearLayout mApply;
    private LinearLayout mSave;
    private ImageView mApplyIcon;
    private ImageView mSaveIcon;

    private String mName;
    private String mUrl;

    private static final String TAG = "candybar.dialog.wallpaper.options";

    private static final String NAME = "name";
    private static final String URL = "url";

    private static WallpaperOptionsFragment newInstance(String url, String name) {
        WallpaperOptionsFragment fragment = new WallpaperOptionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(URL, url);
        bundle.putString(NAME, name);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showWallpaperOptionsDialog(FragmentManager fm, String url, String name) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = WallpaperOptionsFragment.newInstance(url, name);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {}
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_wallpaper_options, false);
        MaterialDialog dialog = builder.build();
        dialog.show();

        mApply = (LinearLayout) dialog.findViewById(R.id.apply);
        mSave = (LinearLayout) dialog.findViewById(R.id.save);
        mApplyIcon = (ImageView) dialog.findViewById(R.id.apply_icon);
        mSaveIcon = (ImageView) dialog.findViewById(R.id.save_icon);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mName = getArguments().getString(NAME);
        mUrl = getArguments().getString(URL);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString(URL);
            mName = savedInstanceState.getString(NAME);
        }

        int color = ColorHelper.getAttributeColor(getActivity(), android.R.attr.textColorPrimary);
        mApplyIcon.setImageDrawable(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_toolbar_apply, color));
        mSaveIcon.setImageDrawable(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_toolbar_save, color));

        mApply.setOnClickListener(this);
        if (getActivity().getResources().getBoolean(R.bool.enable_wallpaper_download)) {
            mSave.setOnClickListener(this);
            return;
        }
        mSave.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        int color = ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent);
        if (id == R.id.apply) {
            WallpaperHelper.applyWallpaper(getActivity(), null, color, mUrl, mName);
        } else if (id == R.id.save) {
            if (PermissionHelper.isPermissionStorageGranted(getActivity())) {
                File target = new File(WallpaperHelper.getDefaultWallpapersDirectory(getActivity()).toString()
                        + File.separator + mName + FileHelper.IMAGE_EXTENSION);

                if (target.exists()) {
                    Context context = getActivity();
                    CafeBar.builder(getActivity())
                            .theme(new CafeBarTheme.Custom(ColorHelper.getAttributeColor(getActivity(), R.attr.card_background)))
                            .autoDismiss(false)
                            .maxLines(4)
                            .content(String.format(getResources().getString(R.string.wallpaper_download_exist),
                                    ("\"" +mName + FileHelper.IMAGE_EXTENSION+ "\"")))
                            .icon(R.drawable.ic_toolbar_download)
                            .positiveText(R.string.wallpaper_download_exist_replace)
                            .positiveColor(color)
                            .positiveTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Font-Bold.ttf"))
                            .onPositive(cafeBar -> {
                                if (context == null) {
                                    cafeBar.dismiss();
                                    return;
                                }

                                WallpaperHelper.downloadWallpaper(context, color, mUrl, mName);
                                cafeBar.dismiss();
                            })
                            .negativeText(R.string.wallpaper_download_exist_new)
                            .negativeTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Font-Bold.ttf"))
                            .onNegative(cafeBar -> {
                                if (context == null) {
                                    cafeBar.dismiss();
                                    return;
                                }

                                WallpaperHelper.downloadWallpaper(context, color, mUrl, mName +"_"+ System.currentTimeMillis());
                                cafeBar.dismiss();
                            })
                            .build().show();
                    dismiss();
                    return;
                }

                WallpaperHelper.downloadWallpaper(getActivity(), color, mUrl, mName);
                dismiss();
                return;
            }
            PermissionHelper.requestStoragePermission(getActivity());
        }
        dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(URL, mUrl);
        outState.putString(NAME, mName);
        super.onSaveInstanceState(outState);
    }

}
