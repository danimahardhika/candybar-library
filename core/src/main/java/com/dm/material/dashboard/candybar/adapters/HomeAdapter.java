package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.fragments.dialog.IconPreviewFragment;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Home;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.List;

import me.grantland.widget.AutofitTextView;

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

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private Context mContext;
    private List<Home> mHomes;

    private int mItemsCount;
    private int mOrientation;
    private boolean mShowWallpapers;
    private boolean mShowIconRequest;
    private boolean mShowMoreApps;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT = 1;
    private static final int TYPE_ICON_REQUEST = 2;
    private static final int TYPE_WALLPAPERS = 3;
    private static final int TYPE_MORE_APPS = 4;

    public HomeAdapter(@NonNull Context context, @NonNull List<Home> homes, int orientation) {
        mContext = context;
        mHomes = homes;
        mOrientation = orientation;

        mItemsCount = 1;
        if (WallpaperHelper.getWallpaperType(mContext) == WallpaperHelper.CLOUD_WALLPAPERS) {
            mItemsCount += 1;
            mShowWallpapers = true;
        }

        if (mContext.getResources().getBoolean(R.bool.enable_icon_request) ||
                mContext.getResources().getBoolean(R.bool.enable_premium_request)) {
            mItemsCount += 1;
            mShowIconRequest = true;
        }

        String link = mContext.getResources().getString(R.string.google_play_dev);
        if (link.length() > 0) {
            mItemsCount += 1;
            mShowMoreApps = true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_header, parent, false);
        } else if (viewType == TYPE_CONTENT) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_content, parent, false);
        } else if (viewType == TYPE_ICON_REQUEST) {
            view =  LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_icon_request, parent, false);
        } else if (viewType == TYPE_WALLPAPERS) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_wallpapers, parent, false);
        } else if (viewType == TYPE_MORE_APPS) {
            view =  LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_more_apps, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.holderId == TYPE_CONTENT) {
            holder.autoFitTitle.setSingleLine(false);
            holder.autoFitTitle.setMaxLines(10);
            holder.autoFitTitle.setSizeToFit(false);
            holder.autoFitTitle.setGravity(Gravity.CENTER_VERTICAL);
            holder.autoFitTitle.setIncludeFontPadding(true);
            holder.autoFitTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            holder.subtitle.setVisibility(View.GONE);
            holder.subtitle.setGravity(Gravity.CENTER_VERTICAL);

        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            if (holder.itemView != null) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams)
                        holder.itemView.getLayoutParams();

                if (holder.holderId == TYPE_HEADER && mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    layoutParams.setFullSpan(true);
                } else {
                    layoutParams.setFullSpan(false);
                }
            }
        } catch (Exception e) {
            LogUtil.d(Log.getStackTraceString(e));
        }

        if (holder.holderId == TYPE_HEADER) {
            holder.title.setText(mContext.getResources().getString(R.string.home_title));
            holder.content.setHtml(mContext.getResources().getString(R.string.home_description));

            String uri = mContext.getResources().getString(R.string.home_image);
            if (URLUtil.isValidUrl(uri)) {
                ImageLoader.getInstance().displayImage(uri,
                        holder.image, ImageConfig.getDefaultImageOptions(true));
            } else if (ColorHelper.isValidColor(uri)) {
                holder.image.setBackgroundColor(Color.parseColor(uri));
            } else {
                uri = "drawable://" + DrawableHelper.getResourceId(mContext, uri);

                ImageLoader.getInstance().displayImage(uri,
                        holder.image, ImageConfig.getDefaultImageOptions(true));
            }
        } else if (holder.holderId == TYPE_CONTENT) {
            int finalPosition = position - 1;

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            if (mHomes.get(finalPosition).getIcon() != -1) {
                if (mHomes.get(finalPosition).getType() == Home.Type.DIMENSION) {
                    holder.autoFitTitle.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getResizedDrawable(
                            mContext, mHomes.get(finalPosition).getIcon(), R.dimen.icon_size_medium), null, null, null);
                } else {
                    holder.autoFitTitle.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                            mContext, mHomes.get(finalPosition).getIcon(), color), null, null, null);
                }
            }

            if (mHomes.get(finalPosition).getType() == Home.Type.ICONS) {
                holder.autoFitTitle.setSingleLine(true);
                holder.autoFitTitle.setMaxLines(1);
                holder.autoFitTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        mContext.getResources().getDimension(R.dimen.text_max_size));
                holder.autoFitTitle.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                holder.autoFitTitle.setIncludeFontPadding(false);
                holder.autoFitTitle.setSizeToFit(true);

                holder.subtitle.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            } else {
                holder.autoFitTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources()
                        .getDimension(R.dimen.text_content_title));
            }

            holder.autoFitTitle.setText(mHomes.get(finalPosition).getTitle());

            if (mHomes.get(finalPosition).getSubtitle().length() > 0) {
                holder.subtitle.setText(mHomes.get(finalPosition).getSubtitle());
                holder.subtitle.setVisibility(View.VISIBLE);
            }
        } else if (holder.holderId == TYPE_ICON_REQUEST) {
            int installed = CandyBarMainActivity.sInstalledAppsCount;
            int missed = CandyBarMainActivity.sMissedApps == null ?
                    installed : CandyBarMainActivity.sMissedApps.size();
            int themed = installed - missed;

            holder.installedApps.setText(String.format(
                    mContext.getResources().getString(R.string.home_icon_request_installed_apps),
                    installed));
            holder.missedApps.setText(String.format(
                    mContext.getResources().getString(R.string.home_icon_request_missed_apps),
                    missed));
            holder.themedApps.setText(String.format(
                    mContext.getResources().getString(R.string.home_icon_request_themed_apps),
                    themed));

            holder.progress.setMax(installed);
            holder.progress.setProgress(themed);
        } else if (holder.holderId == TYPE_WALLPAPERS) {
            holder.title.setText(
                    String.format(mContext.getResources().getString(R.string.home_loud_wallpapers),
                    Preferences.getPreferences(mContext).getAvailableWallpapersCount()));
        }
    }

    @Override
    public int getItemCount() {
        return mHomes.size() + mItemsCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == (mHomes.size() + 1) && mShowIconRequest) return TYPE_ICON_REQUEST;

        if (position == (getItemCount() - 2) && mShowWallpapers && mShowMoreApps)
            return TYPE_WALLPAPERS;

        if (position == (getItemCount() - 1)) {
            if (mShowMoreApps) return TYPE_MORE_APPS;
            else if (mShowWallpapers) return TYPE_WALLPAPERS;
        }
        return TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        ImageView share;
        TextView title;
        TextView subtitle;
        TextView muzei;
        TextView installedApps;
        TextView themedApps;
        TextView missedApps;
        HtmlTextView content;
        AutofitTextView autoFitTitle;
        AppCompatButton rate;
        LinearLayout container;
        ProgressBar progress;

        int holderId;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == TYPE_HEADER) {
                image = (ImageView) itemView.findViewById(R.id.header_image);
                title = (TextView) itemView.findViewById(R.id.title);
                content = (HtmlTextView) itemView.findViewById(R.id.content);
                rate = (AppCompatButton) itemView.findViewById(R.id.rate);
                share = (ImageView) itemView.findViewById(R.id.share);
                holderId = TYPE_HEADER;

                int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
                rate.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                        mContext, R.drawable.ic_toolbar_rate, color), null, null, null);
                share.setImageDrawable(DrawableHelper.getTintedDrawable(
                        mContext, R.drawable.ic_toolbar_share, color));

                rate.setOnClickListener(this);
                share.setOnClickListener(this);
            } else if (viewType == TYPE_CONTENT) {
                container = (LinearLayout) itemView.findViewById(R.id.container);
                autoFitTitle = (AutofitTextView) itemView.findViewById(R.id.title);
                subtitle = (TextView) itemView.findViewById(R.id.subtitle);
                holderId = TYPE_CONTENT;

                container.setOnClickListener(this);
            } else if (viewType == TYPE_ICON_REQUEST) {
                title = (TextView) itemView.findViewById(R.id.title);
                installedApps = (TextView) itemView.findViewById(R.id.installed_apps);
                missedApps = (TextView) itemView.findViewById(R.id.missed_apps);
                themedApps = (TextView) itemView.findViewById(R.id.themed_apps);
                progress = (ProgressBar) itemView.findViewById(R.id.progress);
                container = (LinearLayout) itemView.findViewById(R.id.container);
                holderId = TYPE_ICON_REQUEST;

                int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                        mContext, R.drawable.ic_toolbar_icon_request, color), null, null, null);

                int accent = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);
                progress.getProgressDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);

                container.setOnClickListener(this);
            } else if (viewType == TYPE_WALLPAPERS) {
                title = (TextView) itemView.findViewById(R.id.title);
                muzei = (TextView) itemView.findViewById(R.id.muzei);
                holderId = TYPE_WALLPAPERS;

                int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                        mContext, R.drawable.ic_toolbar_wallpapers, color), null, null, null);

                muzei.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getDrawable(
                        mContext, R.drawable.ic_home_app_muzei), null, null, null);

                title.setOnClickListener(this);
                muzei.setOnClickListener(this);
            } else if (viewType == TYPE_MORE_APPS) {
                container = (LinearLayout) itemView.findViewById(R.id.container);
                title = (TextView) itemView.findViewById(R.id.title);
                subtitle = (TextView) itemView.findViewById(R.id.subtitle);
                holderId = TYPE_MORE_APPS;

                int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                        mContext, R.drawable.ic_google_play_more_apps, color), null, null, null);

                container.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.rate) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?id=" + mContext.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                mContext.startActivity(intent);
            } else if (id == R.id.share) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        String.format(mContext.getResources().getString(R.string.share_app_title),
                                mContext.getResources().getString(R.string.app_name)));
                intent.putExtra(android.content.Intent.EXTRA_TEXT,
                        String.format(mContext.getResources().getString(R.string.share_app_content),
                                "https://play.google.com/store/apps/details?id=" + mContext.getPackageName()));
                mContext.startActivity(Intent.createChooser(intent,
                        mContext.getResources().getString(R.string.email_client)));
            } else if (id == R.id.container) {
                if (holderId == TYPE_CONTENT) {
                    int position = getAdapterPosition() - 1;
                    if (position < 0 || position > mHomes.size()) return;

                    switch (mHomes.get(position).getType()) {
                        case APPLY:
                            ((CandyBarMainActivity) mContext).selectPosition(1);
                            break;
                        case DONATE:
                            if (mContext instanceof CandyBarMainActivity) {
                                CandyBarMainActivity mainActivity = (CandyBarMainActivity) mContext;
                                mainActivity.showSupportDevelopmentDialog();
                            }
                            break;
                        case ICONS:
                            ((CandyBarMainActivity) mContext).selectPosition(2);
                            break;
                        case DIMENSION:
                            Home home = mHomes.get(position);
                            IconPreviewFragment.showIconPreview(
                                    ((AppCompatActivity) mContext).getSupportFragmentManager(),
                                    home.getTitle(), home.getIcon());
                            break;
                    }
                } else if (holderId == TYPE_ICON_REQUEST) {
                    ((CandyBarMainActivity) mContext).selectPosition(3);
                } else if (holderId == TYPE_MORE_APPS) {
                    String link = mContext.getResources().getString(R.string.google_play_dev);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    mContext.startActivity(intent);
                }
            } else if (id == R.id.title) {
                if (holderId == TYPE_WALLPAPERS) {
                    ((CandyBarMainActivity) mContext).selectPosition(4);
                }
            } else if (id == R.id.muzei) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?id=net.nurik.roman.muzei"));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                mContext.startActivity(intent);
            }
        }
    }

    public void addNewContent(@Nullable Home home) {
        if (home == null) return;

        mHomes.add(home);
        notifyItemInserted(mHomes.size());
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
        notifyDataSetChanged();
    }
}
