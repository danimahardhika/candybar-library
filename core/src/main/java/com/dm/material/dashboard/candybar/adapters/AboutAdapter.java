package com.dm.material.dashboard.candybar.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.fragments.dialog.CreditsFragment;
import com.dm.material.dashboard.candybar.fragments.dialog.LicensesFragment;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
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

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder> {

    private final Context mContext;

    private int mItemCount;

    private boolean mCardMode;
    private boolean mShowContributors;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SUBHEADER = 1;
    private static final int TYPE_SUBFOOTER = 2;
    private static final int TYPE_FOOTER = 3;
    private static final int TYPE_BOTTOM_SHADOW = 4;

    public AboutAdapter(@NonNull Context context, int spanCount) {
        mContext = context;

        mItemCount = 3;
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_about_item_header, parent, false);
        } else if (viewType == TYPE_SUBHEADER || viewType == TYPE_SUBFOOTER) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_about_item_sub, parent, false);
        } else if (viewType == TYPE_FOOTER) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_about_item_footer, parent, false);
        } else if (viewType == TYPE_BOTTOM_SHADOW) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_settings_item_footer, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.holderId == TYPE_HEADER) {
            String imageUri = mContext.getString(R.string.about_image);

            if (ColorHelper.isValidColor(imageUri)) {
                holder.image.setBackgroundColor(Color.parseColor(imageUri));
            } else if (!URLUtil.isValidUrl(imageUri)) {
                imageUri = "drawable://" + DrawableHelper.getResourceId(mContext, imageUri);
                ImageLoader.getInstance().displayImage(imageUri, holder.image,
                        ImageConfig.getDefaultImageOptions(true));
            } else {
                ImageLoader.getInstance().displayImage(imageUri, holder.image,
                        ImageConfig.getDefaultImageOptions(true));
            }

            String profileUri = mContext.getResources().getString(R.string.about_profile_image);
            if (!URLUtil.isValidUrl(profileUri)) {
                profileUri = "drawable://" + DrawableHelper.getResourceId(mContext, imageUri);
            }

            ImageLoader.getInstance().displayImage(profileUri, holder.profile,
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
            if (mShowContributors) return TYPE_SUBHEADER;
            else return TYPE_SUBFOOTER;
        }

        if (position == 2) {
            if (mShowContributors) return TYPE_SUBFOOTER;
            else return TYPE_FOOTER;
        }

        if (position == 3 && mShowContributors && !mCardMode) return TYPE_FOOTER;
        return TYPE_BOTTOM_SHADOW;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        CircularImageView profile;

        TextView title;
        HtmlTextView subtitle;
        AppCompatButton email;
        AppCompatButton link1;
        AppCompatButton link2;

        int holderId;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == TYPE_HEADER) {
                image = (ImageView) itemView.findViewById(R.id.image);
                profile = (CircularImageView) itemView.findViewById(R.id.profile);
                subtitle = (HtmlTextView) itemView.findViewById(R.id.subtitle);
                email = (AppCompatButton) itemView.findViewById(R.id.email);
                link1 = (AppCompatButton) itemView.findViewById(R.id.link1);
                link2 = (AppCompatButton) itemView.findViewById(R.id.link2);
                holderId = TYPE_HEADER;

                subtitle.setHtml(mContext.getResources().getString(R.string.about_desc));

                if (!mCardMode) {
                    int primary = ColorHelper.getAttributeColor(mContext, R.attr.colorPrimary);

                    int card = ColorHelper.getAttributeColor(mContext, R.attr.card_background);
                    email.setTextColor(ColorHelper.getTitleTextColor(primary));
                    link1.setTextColor(ColorHelper.getTitleTextColor(card));
                    link2.setTextColor(ColorHelper.getTitleTextColor(card));
                }

                String emailText = mContext.getResources().getString(R.string.about_email);
                if (emailText.length() == 0) email.setVisibility(View.GONE);
                String link2Text = mContext.getResources().getString(R.string.about_link_2_url);
                if (link2Text.length() == 0) link2.setVisibility(View.GONE);

                email.setOnClickListener(this);
                link1.setOnClickListener(this);
                link2.setOnClickListener(this);
            } else if (viewType == TYPE_SUBHEADER) {
                title = (TextView) itemView.findViewById(R.id.title);
                holderId = TYPE_SUBHEADER;

                int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                        mContext, R.drawable.ic_toolbar_people, color), null, null, null);
                title.setText(mContext.getResources().getString(R.string.about_contributors_title));

                title.setOnClickListener(this);
            } else if (viewType == TYPE_SUBFOOTER) {
                title = (TextView) itemView.findViewById(R.id.title);
                holderId = TYPE_SUBFOOTER;

                int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                        mContext, R.drawable.ic_toolbar_licenses, color), null, null, null);
                title.setText(mContext.getResources().getString(R.string.about_open_source_licenses));

                title.setOnClickListener(this);
            } else if (viewType == TYPE_FOOTER) {
                link2 = (AppCompatButton) itemView.findViewById(R.id.dev_google_plus);
                link1 = (AppCompatButton) itemView.findViewById(R.id.dev_github);
                holderId = TYPE_FOOTER;

                if (!mCardMode) {
                    int primary = ColorHelper.getAttributeColor(mContext, R.attr.colorPrimary);
                    int card = ColorHelper.getAttributeColor(mContext, R.attr.card_background);
                    link1.setTextColor(ColorHelper.getTitleTextColor(primary));
                    link2.setTextColor(ColorHelper.getTitleTextColor(card));
                }

                link1.setOnClickListener(this);
                link2.setOnClickListener(this);
            } else if (viewType == TYPE_BOTTOM_SHADOW) {
                holderId = TYPE_BOTTOM_SHADOW;
            }
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
            } else if (id == R.id.title) {
                if (holderId == TYPE_SUBHEADER) {
                    CreditsFragment.showCreditsDialog(((AppCompatActivity) mContext).getSupportFragmentManager());
                } else if (holderId == TYPE_SUBFOOTER) {
                    LicensesFragment.showLicensesDialog(((AppCompatActivity) mContext).getSupportFragmentManager());
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
            } else if (id == R.id.dev_google_plus) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext
                        .getResources().getString(R.string.about_dashboard_dev_google_plus_url)));
            } else if (id == R.id.dev_github) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext
                        .getResources().getString(R.string.about_dashboard_dev_github_url)));
            }

            try {
                mContext.startActivity(intent);
            } catch (NullPointerException | ActivityNotFoundException e) {
                LogUtil.e(Log.getStackTraceString(e));
            }
        }
    }
}
