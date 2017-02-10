package com.dm.material.dashboard.candybar.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.fragments.dialog.CreditsFragment;
import com.dm.material.dashboard.candybar.fragments.dialog.LicensesFragment;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.sufficientlysecure.htmltextview.HtmlTextView;

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

public class AboutFragment extends Fragment implements View.OnClickListener {

    private NestedScrollView mScrollView;
    private ImageView mImageView;
    private HtmlTextView mAboutDesc;
    private LinearLayout mContributors;
    private ImageView mContributorsIcon;
    private LinearLayout mLicenses;
    private ImageView mLicensesIcon;
    private TextView mEmail;
    private TextView mLink1;
    private TextView mLink2;
    private TextView mDevGooglePlus;
    private TextView mDevGitHub;
    private CircularImageView mPrrofile;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        mScrollView = (NestedScrollView) view.findViewById(R.id.scrollview);
        mImageView = (ImageView) view.findViewById(R.id.image);
        mAboutDesc = (HtmlTextView) view.findViewById(R.id.about_desc);
        mContributors = (LinearLayout) view.findViewById(R.id.contributors);
        mContributorsIcon = (ImageView) view.findViewById(R.id.contributors_icon);
        mLicenses = (LinearLayout) view.findViewById(R.id.licenses);
        mLicensesIcon = (ImageView) view.findViewById(R.id.licenses_icon);
        mEmail = (TextView) view.findViewById(R.id.email);
        mLink1 = (TextView) view.findViewById(R.id.link1);
        mLink2 = (TextView) view.findViewById(R.id.link2);
        mDevGooglePlus = (TextView) view.findViewById(R.id.dev_google_plus);
        mDevGitHub = (TextView) view.findViewById(R.id.dev_github);
        mPrrofile = (CircularImageView) view.findViewById(R.id.profile);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mScrollView, false);
        ViewHelper.resetNavigationBarBottomPadding(getActivity(), mScrollView,
                getActivity().getResources().getConfiguration().orientation);

        initImageHeader();
        initProfileImage();
        initAbout();

        int color = ColorHelper.getAttributeColor(getActivity(), android.R.attr.textColorPrimary);

        mLicensesIcon.setImageDrawable(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_toolbar_licenses, color));

        if (getActivity().getResources().getBoolean(R.bool.show_contributors_dialog)) {
            mContributorsIcon.setImageDrawable(DrawableHelper.getTintedDrawable(
                    getActivity(), R.drawable.ic_toolbar_people, color));
            mContributors.setOnClickListener(this);
            CardView cardView = (CardView) getActivity().findViewById(R.id.card_contributors);
            cardView.setVisibility(View.VISIBLE);
        }

        mLicenses.setOnClickListener(this);

        mEmail.setOnClickListener(this);
        mLink1.setOnClickListener(this);
        mLink2.setOnClickListener(this);
        mDevGooglePlus.setOnClickListener(this);
        mDevGitHub.setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetNavigationBarBottomPadding(getActivity(), mScrollView,
                getActivity().getResources().getConfiguration().orientation);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.contributors) {
            CreditsFragment.showCreditsDialog(getActivity().getSupportFragmentManager());
            return;
        } else if (id == R.id.licenses) {
            LicensesFragment.showLicensesDialog(getActivity().getSupportFragmentManager());
            return;
        }

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
                return;
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
        } else if (id == R.id.dev_github) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getActivity()
                    .getResources().getString(R.string.about_dashboard_dev_github_url)));
        } else if (id == R.id.dev_google_plus) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getActivity()
                    .getResources().getString(R.string.about_dashboard_dev_google_plus_url)));
        }

        try {
            getActivity().startActivity(intent);
        } catch (NullPointerException | ActivityNotFoundException e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }
    }

    private void initAbout() {
        String desc = getActivity().getResources().getString(R.string.about_desc);
        mAboutDesc.setHtml(desc);

        String email = getActivity().getResources().getString(R.string.about_email);
        if (email.length() == 0) mEmail.setVisibility(View.GONE);
        String link2 = getActivity().getResources().getString(R.string.about_link_2_url);
        if (link2.length() == 0) mLink2.setVisibility(View.GONE);
    }

    private void initImageHeader() {
        String url = getActivity().getString(R.string.about_image);
        if (ColorHelper.isValidColor(url)) {
            mImageView.setBackgroundColor(Color.parseColor(url));
            return;
        }

        if (!URLUtil.isValidUrl(url)) {
            url = "drawable://" + DrawableHelper.getResourceId(getActivity(), url);
        }

        ImageLoader.getInstance().displayImage(url, mImageView,
                ImageConfig.getDefaultImageOptions(true));
    }

    private void initProfileImage() {
        String url = getActivity().getResources().getString(R.string.about_profile_image);
        if (!URLUtil.isValidUrl(url)) {
            url = "drawable://" + DrawableHelper.getResourceId(getActivity(), url);
        }

        ImageLoader.getInstance().displayImage(url, mPrrofile,
                ImageConfig.getDefaultImageOptions(true));
    }
}
