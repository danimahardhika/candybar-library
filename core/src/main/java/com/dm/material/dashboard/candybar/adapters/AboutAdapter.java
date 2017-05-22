package com.dm.material.dashboard.candybar.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.fragments.dialog.CreditsFragment;
import com.dm.material.dashboard.candybar.fragments.dialog.LicensesFragment;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.LogUtil;
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

public class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;

    private int mItemCount;

    private final boolean mCardMode;
    private final boolean mShowContributors;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTRIBUTORS = 1;
    private static final int TYPE_FOOTER = 2;
    private static final int TYPE_BOTTOM_SHADOW = 3;

    public AboutAdapter(@NonNull Context context, int spanCount) {
        mContext = context;

        mItemCount = 2;
        mCardMode = (spanCount > 1);
        if (!mCardMode) {
            mItemCount += 1;
        }

        mShowContributors = mContext.getResources().getBoolean(R.bool.show_contributors_dialog);
        if (mShowContributors) {
            mItemCount += 1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_about_item_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_CONTRIBUTORS ) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_about_item_sub, parent, false);
            return new ContributorsViewHolder(view);
        } if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_about_item_footer, parent, false);
            return new FooterViewHolder(view);
        }

        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_settings_item_footer, parent, false);
        return new ShadowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

            String imageUri = mContext.getString(R.string.about_image);

            if (ColorHelper.isValidColor(imageUri)) {
                headerViewHolder.image.setBackgroundColor(Color.parseColor(imageUri));
            } else if (!URLUtil.isValidUrl(imageUri)) {
                imageUri = "drawable://" + DrawableHelper.getResourceId(mContext, imageUri);
                ImageLoader.getInstance().displayImage(imageUri, headerViewHolder.image,
                        ImageConfig.getDefaultImageOptions(true));
            } else {
                ImageLoader.getInstance().displayImage(imageUri, headerViewHolder.image,
                        ImageConfig.getDefaultImageOptions(true));
            }

            String profileUri = mContext.getResources().getString(R.string.about_profile_image);
            if (!URLUtil.isValidUrl(profileUri)) {
                profileUri = "drawable://" + DrawableHelper.getResourceId(mContext, profileUri);
            }

            ImageLoader.getInstance().displayImage(profileUri, headerViewHolder.profile,
                    ImageConfig.getDefaultImageOptions(true));
        }
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == 1) {
            if (mShowContributors) return TYPE_CONTRIBUTORS;
            else return TYPE_FOOTER;
        }

        if (position == 2 && mShowContributors)  return TYPE_FOOTER;
        return TYPE_BOTTOM_SHADOW;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView image;
        private final CircularImageView profile;

        HeaderViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            profile = (CircularImageView) itemView.findViewById(R.id.profile);
            HtmlTextView subtitle = (HtmlTextView) itemView.findViewById(R.id.subtitle);
            AppCompatButton email = (AppCompatButton) itemView.findViewById(R.id.email);
            AppCompatButton link1 = (AppCompatButton) itemView.findViewById(R.id.link1);
            AppCompatButton link2 = (AppCompatButton) itemView.findViewById(R.id.link2);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            if (!Preferences.get(mContext).isShadowEnabled()) {
                if (card != null) card.setCardElevation(0);

                profile.setShadowRadius(0f);
                profile.setShadowColor(Color.TRANSPARENT);
            }

            subtitle.setHtml(mContext.getResources().getString(R.string.about_desc));

            if (!mCardMode) {
                int accent = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);

                int cardColor = ColorHelper.getAttributeColor(mContext, R.attr.card_background);
                email.setTextColor(ColorHelper.getTitleTextColor(accent));
                link1.setTextColor(ColorHelper.getTitleTextColor(cardColor));
                link2.setTextColor(ColorHelper.getTitleTextColor(cardColor));
            }

            String emailText = mContext.getResources().getString(R.string.about_email);
            if (emailText.length() == 0) email.setVisibility(View.GONE);
            String link2Text = mContext.getResources().getString(R.string.about_link_2_url);
            if (link2Text.length() == 0) link2.setVisibility(View.GONE);

            email.setOnClickListener(this);
            link1.setOnClickListener(this);
            link2.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.email) {
                try {
                    final Intent email = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", mContext.getResources().getString(
                                    R.string.about_email), null));
                    email.putExtra(Intent.EXTRA_SUBJECT, (mContext.getResources().getString(
                            R.string.app_name)));
                    mContext.startActivity(Intent.createChooser(email,
                            mContext.getResources().getString(R.string.email_client)));
                    return;
                }
                catch (ActivityNotFoundException e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
                return;
            }

            Intent intent = null;
            if (id == R.id.link1) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        mContext.getResources().getString(R.string.about_link_1_url)));
            } else if (id == R.id.link2) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        mContext.getResources().getString(R.string.about_link_2_url)));
            }

            try {
                mContext.startActivity(intent);
            } catch (NullPointerException | ActivityNotFoundException e) {
                LogUtil.e(Log.getStackTraceString(e));
            }
        }
    }

    private class ContributorsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ContributorsViewHolder(View itemView) {
            super(itemView);
            TextView title = (TextView) itemView.findViewById(R.id.title);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            if (!Preferences.get(mContext).isShadowEnabled()) {
                if (card != null) card.setCardElevation(0);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_people, color), null, null, null);
            title.setText(mContext.getResources().getString(R.string.about_contributors_title));

            title.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            CreditsFragment.showCreditsDialog(((AppCompatActivity) mContext).getSupportFragmentManager(),
                    CreditsFragment.TYPE_ICON_PACK_CONTRIBUTORS);
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        FooterViewHolder(View itemView) {
            super(itemView);
            AppCompatButton link1 = (AppCompatButton) itemView.findViewById(R.id.about_dev_github);
            AppCompatButton link2 = (AppCompatButton) itemView.findViewById(R.id.about_dev_google_plus);
            TextView licenses = (TextView) itemView.findViewById(R.id.about_dashboard_licenses);
            TextView contributors = (TextView) itemView.findViewById(R.id.about_dashboard_contributors);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            if (!Preferences.get(mContext).isShadowEnabled()) {
                if (card != null) card.setCardElevation(0);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            TextView title = (TextView) itemView.findViewById(R.id.about_dashboard_title);
            title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_dashboard, color), null, null, null);

            if (!mCardMode) {
                int accent = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);
                int cardColor = ColorHelper.getAttributeColor(mContext, R.attr.card_background);
                link1.setTextColor(ColorHelper.getTitleTextColor(accent));
                link2.setTextColor(ColorHelper.getTitleTextColor(cardColor));
            }

            link1.setOnClickListener(this);
            link2.setOnClickListener(this);
            licenses.setOnClickListener(this);
            contributors.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.about_dashboard_licenses) {
                LicensesFragment.showLicensesDialog(((AppCompatActivity) mContext).getSupportFragmentManager());
                return;
            }

            if (id == R.id.about_dashboard_contributors) {
                CreditsFragment.showCreditsDialog(((AppCompatActivity) mContext).getSupportFragmentManager(),
                        CreditsFragment.TYPE_DASHBOARD_CONTRIBUTORS);
                return;
            }

            Intent intent = null;
            if (id == R.id.about_dev_github) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext
                        .getResources().getString(R.string.about_dashboard_dev_github_url)));
            } else if (id == R.id.about_dev_google_plus) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext
                        .getResources().getString(R.string.about_dashboard_dev_google_plus_url)));
            }

            try {
                mContext.startActivity(intent);
            } catch (NullPointerException | ActivityNotFoundException e) {
                LogUtil.e(Log.getStackTraceString(e));
            }
        }
    }

    private class ShadowViewHolder extends RecyclerView.ViewHolder {

        ShadowViewHolder(View itemView) {
            super(itemView);
            if (!Preferences.get(mContext).isShadowEnabled()) {
                View shadow = itemView.findViewById(R.id.shadow);
                shadow.setVisibility(View.GONE);
            }
        }
    }
}
