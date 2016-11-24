package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
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

public class AboutFragment extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "candybar.dialog.about";

    private static AboutFragment newInstance() {
        return new AboutFragment();
    }

    public static void showAbout(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment dialog = AboutFragment.newInstance();
        dialog.show(ft, TAG);
    }

    private ImageView mImageView;
    private CircularImageView mProfile;
    private MaterialRippleLayout mEmailContainer;
    private TextView mEmail;
    private TextView mLink1;
    private MaterialRippleLayout mLink2Container;
    private TextView mLink2;
    private TextView mDevLink1;
    private TextView mDevLink2;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_about, false);
        MaterialDialog dialog = builder.build();
        dialog.show();

        mImageView = (ImageView) dialog.findViewById(R.id.image);
        mProfile = (CircularImageView) dialog.findViewById(R.id.profile);
        mEmailContainer = (MaterialRippleLayout) dialog.findViewById(R.id.container_email);
        mEmail = (TextView) dialog.findViewById(R.id.email);
        mLink1 = (TextView) dialog.findViewById(R.id.link1);
        mLink2Container = (MaterialRippleLayout) dialog.findViewById(R.id.container_link2);
        mLink2 = (TextView) dialog.findViewById(R.id.link2);
        mDevLink1 = (TextView) dialog.findViewById(R.id.dev_link1);
        mDevLink2 = (TextView) dialog.findViewById(R.id.dev_link2);

        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initImageHeader();
        initProfileImage();
        initAbout();

        mEmail.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.button_accent_dark : R.drawable.button_accent);
        mLink1.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.button_accent_dark : R.drawable.button_accent);
        mLink2.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.button_accent_dark : R.drawable.button_accent);

        mDevLink1.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.button_accent_dark : R.drawable.button_accent);
        mDevLink2.setBackgroundResource(Preferences.getPreferences(getActivity()).isDarkTheme() ?
                R.drawable.button_accent_dark : R.drawable.button_accent);

        mEmail.setOnClickListener(this);
        mLink1.setOnClickListener(this);
        mLink2.setOnClickListener(this);
        mDevLink1.setOnClickListener(this);
        mDevLink2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent = null;
        if (id == R.id.email) {
            try {
                final Intent email = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", getActivity().getResources().getString(
                                R.string.about_email), null));
                email.putExtra(Intent.EXTRA_SUBJECT, (getActivity().getResources().getString(
                        R.string.app_name)));
                getActivity().startActivity(Intent.createChooser(email,
                        getActivity().getResources().getString(R.string.email_client)));
                dismiss();
            }
            catch (ActivityNotFoundException e) {
                Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
            }
            return;
        } else if (id == R.id.link1) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    getActivity().getResources().getString(R.string.about_link_1_url)));
        } else if (id == R.id.link2) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    getActivity().getResources().getString(R.string.about_link_2_url)));
        } else if (id == R.id.dev_link1) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getActivity()
                    .getResources().getString(R.string.about_dashboard_dev_github_url)));
        } else if (id == R.id.dev_link2) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getActivity()
                    .getResources().getString(R.string.about_dashboard_dev_google_plus_url)));
        }

        try {
            getActivity().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
    }

    private void initImageHeader() {
        String url = getActivity().getString(R.string.about_image);
        if (URLUtil.isValidUrl(url)) {
            ImageLoader.getInstance().displayImage(url, mImageView,
                    ImageConfig.getImageOptions(true, Preferences.getPreferences(getActivity())
                            .isCacheAllowed()));
        } else {
            int res = DrawableHelper.getResourceId(getActivity(), url);
            if (res > 0) mImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), res));
        }
    }

    private void initProfileImage() {
        String url = getActivity().getResources().getString(R.string.about_profile_image);
        if (URLUtil.isValidUrl(url)) {
            ImageLoader.getInstance().displayImage(url, mProfile,
                    ImageConfig.getImageOptions(true, Preferences.getPreferences(getActivity())
                    .isCacheAllowed()));
        } else {
            int res = DrawableHelper.getResourceId(getActivity(), url);
            if (res > 0) mProfile.setImageDrawable(ContextCompat.getDrawable(getActivity(), res));
        }
    }

    private void initAbout() {
        String email = getActivity().getResources().getString(R.string.about_email);
        if (email.length() == 0) mEmailContainer.setVisibility(View.GONE);
        String link2 = getActivity().getResources().getString(R.string.about_link_2_url);
        if (link2.length() == 0) mLink2Container.setVisibility(View.GONE);
    }

}
