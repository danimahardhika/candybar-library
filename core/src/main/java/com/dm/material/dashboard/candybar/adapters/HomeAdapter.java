package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
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

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.fragments.dialog.IconPreviewFragment;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Home;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.dm.material.dashboard.candybar.utils.views.HeaderView;
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

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Home> mHomes;
    private final Home.Style mImageStyle;

    private int mItemsCount;
    private int mOrientation;
    private boolean mShowWallpapers;
    private boolean mShowIconRequest;
    private boolean mShowMoreApps;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT = 1;
    private static final int TYPE_ICON_REQUEST = 2;
    private static final int TYPE_WALLPAPERS = 3;
    private static final int TYPE_GOOGLE_PLAY_DEV = 4;

    public HomeAdapter(@NonNull Context context, @NonNull List<Home> homes, int orientation) {
        mContext = context;
        mHomes = homes;
        mOrientation = orientation;

        String viewStyle = mContext.getResources().getString(R.string.home_image_style);
        mImageStyle = ViewHelper.getHomeImageViewStyle(viewStyle);

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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_header, parent, false);
            if (mImageStyle.getType() == Home.Style.Type.LANDSCAPE ||
                    mImageStyle.getType() == Home.Style.Type.SQUARE) {
                view = LayoutInflater.from(mContext).inflate(
                        R.layout.fragment_home_item_header_alt, parent, false);
            }
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_CONTENT) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_content, parent, false);
            return new ContentViewHolder(view);
        } else if (viewType == TYPE_ICON_REQUEST) {
            View view =  LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_icon_request, parent, false);
            return new IconRequestViewHolder(view);
        } else if (viewType == TYPE_WALLPAPERS) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_wallpapers, parent, false);
            return new WallpapersViewHolder(view);
        }

        View view =  LayoutInflater.from(mContext).inflate(
                R.layout.fragment_home_item_more_apps, parent, false);
        return new GooglePlayDevViewHolder(view);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.getItemViewType() == TYPE_CONTENT) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;

            contentViewHolder.autoFitTitle.setSingleLine(false);
            contentViewHolder.autoFitTitle.setMaxLines(10);
            contentViewHolder.autoFitTitle.setSizeToFit(false);
            contentViewHolder.autoFitTitle.setGravity(Gravity.CENTER_VERTICAL);
            contentViewHolder.autoFitTitle.setIncludeFontPadding(true);
            contentViewHolder.autoFitTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            contentViewHolder.subtitle.setVisibility(View.GONE);
            contentViewHolder.subtitle.setGravity(Gravity.CENTER_VERTICAL);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder.itemView != null) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams)
                        holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(isFullSpan(holder.getItemViewType()));
            }
        } catch (Exception e) {
            LogUtil.d(Log.getStackTraceString(e));
        }

        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

            headerViewHolder.title.setText(mContext.getResources().getString(R.string.home_title));
            headerViewHolder.content.setHtml(mContext.getResources().getString(R.string.home_description));

            String uri = mContext.getResources().getString(R.string.home_image);
            if (URLUtil.isValidUrl(uri)) {
                ImageLoader.getInstance().displayImage(uri,
                        headerViewHolder.image, ImageConfig.getDefaultImageOptions(true));
            } else if (ColorHelper.isValidColor(uri)) {
                headerViewHolder.image.setBackgroundColor(Color.parseColor(uri));
            } else {
                uri = "drawable://" + DrawableHelper.getResourceId(mContext, uri);

                ImageLoader.getInstance().displayImage(uri,
                        headerViewHolder.image, ImageConfig.getDefaultImageOptions(true));
            }
        } else if (holder.getItemViewType() == TYPE_CONTENT) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            int finalPosition = position - 1;

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            if (mHomes.get(finalPosition).getIcon() != -1) {
                if (mHomes.get(finalPosition).getType() == Home.Type.DIMENSION) {
                    contentViewHolder.autoFitTitle.setCompoundDrawablesWithIntrinsicBounds(
                            DrawableHelper.getResizedDrawable(mContext,
                                    DrawableHelper.get(mContext, mHomes.get(finalPosition).getIcon()),
                                    40),
                            null, null, null);
                } else {
                    contentViewHolder.autoFitTitle.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                            mContext, mHomes.get(finalPosition).getIcon(), color), null, null, null);
                }
            }

            if (mHomes.get(finalPosition).getType() == Home.Type.ICONS) {
                contentViewHolder.autoFitTitle.setSingleLine(true);
                contentViewHolder.autoFitTitle.setMaxLines(1);
                contentViewHolder.autoFitTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        mContext.getResources().getDimension(R.dimen.text_max_size));
                contentViewHolder.autoFitTitle.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                contentViewHolder.autoFitTitle.setIncludeFontPadding(false);
                contentViewHolder.autoFitTitle.setSizeToFit(true);

                contentViewHolder.subtitle.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            } else {
                contentViewHolder.autoFitTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources()
                        .getDimension(R.dimen.text_content_title));
            }

            contentViewHolder.autoFitTitle.setText(mHomes.get(finalPosition).getTitle());

            if (mHomes.get(finalPosition).getSubtitle().length() > 0) {
                contentViewHolder.subtitle.setText(mHomes.get(finalPosition).getSubtitle());
                contentViewHolder.subtitle.setVisibility(View.VISIBLE);
            }
        } else if (holder.getItemViewType() == TYPE_ICON_REQUEST) {
            IconRequestViewHolder iconRequestViewHolder = (IconRequestViewHolder) holder;

            int installed = CandyBarMainActivity.sInstalledAppsCount;
            int missed = CandyBarMainActivity.sMissedApps == null ?
                    installed : CandyBarMainActivity.sMissedApps.size();
            int themed = installed - missed;

            iconRequestViewHolder.installedApps.setText(String.format(
                    mContext.getResources().getString(R.string.home_icon_request_installed_apps),
                    installed));
            iconRequestViewHolder.missedApps.setText(String.format(
                    mContext.getResources().getString(R.string.home_icon_request_missed_apps),
                    missed));
            iconRequestViewHolder.themedApps.setText(String.format(
                    mContext.getResources().getString(R.string.home_icon_request_themed_apps),
                    themed));

            iconRequestViewHolder.progress.setMax(installed);
            iconRequestViewHolder.progress.setProgress(themed);
        } else if (holder.getItemViewType() == TYPE_WALLPAPERS) {
            WallpapersViewHolder wallpapersViewHolder = (WallpapersViewHolder) holder;

            wallpapersViewHolder.title.setText(
                    String.format(mContext.getResources().getString(R.string.home_loud_wallpapers),
                    Preferences.get(mContext).getAvailableWallpapersCount()));
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
            if (mShowMoreApps) return TYPE_GOOGLE_PLAY_DEV;
            else if (mShowWallpapers) return TYPE_WALLPAPERS;
        }
        return TYPE_CONTENT;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final HeaderView image;
        private final TextView title;
        private final HtmlTextView content;

        HeaderViewHolder(View itemView) {
            super(itemView);
            image = (HeaderView) itemView.findViewById(R.id.header_image);
            title = (TextView) itemView.findViewById(R.id.title);
            content = (HtmlTextView) itemView.findViewById(R.id.content);
            AppCompatButton rate = (AppCompatButton) itemView.findViewById(R.id.rate);
            ImageView share = (ImageView) itemView.findViewById(R.id.share);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.setMargins(0, 0, margin, margin);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                } else if (card.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) card.getLayoutParams();
                    if (mImageStyle.getType() == Home.Style.Type.LANDSCAPE ||
                            mImageStyle.getType() == Home.Style.Type.SQUARE) {
                        params.setMargins(margin,
                                mContext.getResources().getDimensionPixelSize(R.dimen.content_padding_reverse),
                                margin, margin);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                }
            }

            if (!Preferences.get(mContext).isShadowEnabled()) {
                card.setCardElevation(0);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
            rate.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_rate, color), null, null, null);
            share.setImageDrawable(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_share, color));

            image.setRatio(mImageStyle.getPoint().x, mImageStyle.getPoint().y);

            rate.setOnClickListener(this);
            share.setOnClickListener(this);
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
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        String.format(mContext.getResources().getString(R.string.share_app_title),
                                mContext.getResources().getString(R.string.app_name)));
                intent.putExtra(Intent.EXTRA_TEXT,
                        mContext.getResources().getString(R.string.share_app_body,
                                mContext.getResources().getString(R.string.app_name),
                                "https://play.google.com/store/apps/details?id=" + mContext.getPackageName()));
                mContext.startActivity(Intent.createChooser(intent,
                        mContext.getResources().getString(R.string.email_client)));
            }
        }
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView subtitle;
        private final AutofitTextView autoFitTitle;
        private final LinearLayout container;

        ContentViewHolder(View itemView) {
            super(itemView);
            container = (LinearLayout) itemView.findViewById(R.id.container);
            autoFitTitle = (AutofitTextView) itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.setMargins(0, 0, margin, margin);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                }
            }

            if (!Preferences.get(mContext).isShadowEnabled()) {
                card.setCardElevation(0);
            }

            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
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
            }
        }
    }

    private class IconRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView title;
        private final TextView installedApps;
        private final TextView themedApps;
        private final TextView missedApps;
        private final LinearLayout container;
        private final ProgressBar progress;

        IconRequestViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            installedApps = (TextView) itemView.findViewById(R.id.installed_apps);
            missedApps = (TextView) itemView.findViewById(R.id.missed_apps);
            themedApps = (TextView) itemView.findViewById(R.id.themed_apps);
            progress = (ProgressBar) itemView.findViewById(R.id.progress);
            container = (LinearLayout) itemView.findViewById(R.id.container);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.setMargins(0, 0, margin, margin);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                }
            }

            if (!Preferences.get(mContext).isShadowEnabled()) {
                card.setCardElevation(0);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_icon_request, color), null, null, null);

            int accent = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);
            progress.getProgressDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);

            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                ((CandyBarMainActivity) mContext).selectPosition(3);
            }
        }
    }

    private class WallpapersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView title;

        WallpapersViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            TextView muzei = (TextView) itemView.findViewById(R.id.muzei);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.setMargins(0, 0, margin, margin);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                }
            }

            if (!Preferences.get(mContext).isShadowEnabled()) {
                card.setCardElevation(0);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_wallpapers, color), null, null, null);

            muzei.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.get(
                    mContext, R.drawable.ic_home_app_muzei), null, null, null);

            title.setOnClickListener(this);
            muzei.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.title) {
                ((CandyBarMainActivity) mContext).selectPosition(4);
            } else if (id == R.id.muzei) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?id=net.nurik.roman.muzei"));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                mContext.startActivity(intent);
            }
        }
    }

    private class GooglePlayDevViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView title;
        private final LinearLayout container;

        GooglePlayDevViewHolder(View itemView) {
            super(itemView);
            container = (LinearLayout) itemView.findViewById(R.id.container);
            title = (TextView) itemView.findViewById(R.id.title);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.setMargins(0, 0, margin, margin);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                }
            }

            if (!Preferences.get(mContext).isShadowEnabled()) {
                card.setCardElevation(0);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_google_play_more_apps, color), null, null, null);

            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                String link = mContext.getResources().getString(R.string.google_play_dev);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                mContext.startActivity(intent);
            }
        }
    }

    public int getApplyIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_CONTENT) {
                int pos = i - 1;
                if (mHomes.get(pos).getType() == Home.Type.APPLY) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public Home getItem(int position) {
        return mHomes.get(position - 1);
    }

    public int getIconsIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_CONTENT) {
                int pos = i - 1;
                if (mHomes.get(pos).getType() == Home.Type.ICONS) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public int getIconRequestIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_ICON_REQUEST) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getWallpapersIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_WALLPAPERS) {
                index = i;
                break;
            }
        }
        return index;
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

    private boolean isFullSpan(int viewType) {
        if (viewType == TYPE_HEADER) {
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                return true;
            } else if (mImageStyle.getType() == Home.Style.Type.SQUARE ||
                    mImageStyle.getType() == Home.Style.Type.LANDSCAPE) {
                return true;
            }
        }
        return false;
    }
}
