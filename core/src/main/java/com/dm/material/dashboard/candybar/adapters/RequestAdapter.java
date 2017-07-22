package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.List;

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

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Request> mRequests;
    private SparseBooleanArray mSelectedItems;
    private final DisplayImageOptions.Builder mOptions;

    private final int mTextColorSecondary;
    private final int mTextColorAccent;
    private boolean mSelectedAll = false;

    private final boolean mShowShadow;
    private final boolean mShowPremiumRequest;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT = 1;
    private static final int TYPE_FOOTER = 2;

    public RequestAdapter(@NonNull Context context, @NonNull List<Request> requests, int spanCount) {
        mContext = context;
        mRequests = requests;
        mTextColorSecondary = ColorHelper.getAttributeColor(mContext,
                android.R.attr.textColorSecondary);
        mTextColorAccent = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);
        mSelectedItems = new SparseBooleanArray();

        mShowShadow = (spanCount == 1);
        mShowPremiumRequest = Preferences.get(mContext).isPremiumRequestEnabled();

        mOptions = ImageConfig.getRawDefaultImageOptions();
        mOptions.resetViewBeforeLoading(true);
        mOptions.cacheInMemory(true);
        mOptions.cacheOnDisk(false);
        mOptions.showImageOnFail(R.drawable.ic_app_default);
        mOptions.displayer(new FadeInBitmapDisplayer(700));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_request_item_header, parent, false);

            StaggeredGridLayoutManager.LayoutParams params = getLayoutParams(view);
            if (params != null) params.setFullSpan(false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_CONTENT) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_request_item_list, parent, false);

            StaggeredGridLayoutManager.LayoutParams params = getLayoutParams(view);
            if (params != null) params.setFullSpan(false);
            return new ContentViewHolder(view);
        }

        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_request_item_footer, parent, false);

        StaggeredGridLayoutManager.LayoutParams params = getLayoutParams(view);
        if (params != null) params.setFullSpan(true);
        return new FooterViewHolder(view);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.getItemViewType() == TYPE_CONTENT) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            contentViewHolder.content.setTextColor(mTextColorSecondary);

            if (mShowShadow) {
                contentViewHolder.divider.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            if (Preferences.get(mContext).isPremiumRequest()) {
                headerViewHolder.button.setVisibility(View.GONE);
                headerViewHolder.content.setVisibility(View.GONE);
                headerViewHolder.container.setVisibility(View.VISIBLE);

                int total = Preferences.get(mContext).getPremiumRequestTotal();
                int available = Preferences.get(mContext).getPremiumRequestCount();

                headerViewHolder.total.setText(String.format(
                        mContext.getResources().getString(R.string.premium_request_count),
                        total));
                headerViewHolder.available.setText(String.format(
                        mContext.getResources().getString(R.string.premium_request_available),
                        available));
                headerViewHolder.used.setText(String.format(
                        mContext.getResources().getString(R.string.premium_request_used),
                        (total - available)));

                headerViewHolder.progress.setMax(total);
                headerViewHolder.progress.setProgress(available);
            } else {
                headerViewHolder.button.setVisibility(View.VISIBLE);
                headerViewHolder.content.setVisibility(View.VISIBLE);
                headerViewHolder.container.setVisibility(View.GONE);
            }
        } else if (holder.getItemViewType() == TYPE_CONTENT) {
            int finalPosition = mShowPremiumRequest ? position - 1 : position;
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;

            ImageLoader.getInstance().displayImage("package://" +mRequests.get(finalPosition).getPackageName(),
                    new ImageViewAware(contentViewHolder.icon), mOptions.build(),
                    new ImageSize(114, 114), null, null);

            contentViewHolder.title.setText(mRequests.get(finalPosition).getName());

            if (mRequests.get(finalPosition).isRequested()) {
                contentViewHolder.content.setTextColor(mTextColorAccent);
                contentViewHolder.content.setText(mContext.getResources().getString(
                        R.string.request_already_requested));
            } else {
                contentViewHolder.content.setText(mContext.getResources().getString(
                        R.string.request_not_requested));
            }

            contentViewHolder.checkbox.setChecked(mSelectedItems.get(finalPosition, false));

            if (finalPosition == (mRequests.size() - 1) && mShowShadow) {
                contentViewHolder.divider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = mRequests == null ? 0 : mRequests.size();
        if (mShowShadow) count += 1;
        if (mShowPremiumRequest) count += 1;
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mShowPremiumRequest) return TYPE_HEADER;
        if (position == (getItemCount() - 1) && mShowShadow) return TYPE_FOOTER;
        return TYPE_CONTENT;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView title;
        private final TextView content;
        private final TextView total;
        private final TextView available;
        private final TextView used;
        private final AppCompatButton button;
        private final LinearLayout container;
        private final ProgressBar progress;

        HeaderViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            content = (TextView) itemView.findViewById(R.id.content);
            button = (AppCompatButton) itemView.findViewById(R.id.buy);

            container = (LinearLayout) itemView.findViewById(R.id.premium_request);
            total = (TextView) itemView.findViewById(R.id.premium_request_total);
            available = (TextView) itemView.findViewById(R.id.premium_request_available);
            used = (TextView) itemView.findViewById(R.id.premium_request_used);
            progress = (ProgressBar) itemView.findViewById(R.id.progress);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getRequestStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT &&
                    card != null) {
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

            if (!Preferences.get(mContext).isShadowEnabled() && card != null) {
                card.setCardElevation(0);
            }

            int padding = mContext.getResources().getDimensionPixelSize(R.dimen.content_margin) +
                    mContext.getResources().getDimensionPixelSize(R.dimen.icon_size_small);
            content.setPadding(padding, 0, 0, 0);
            container.setPadding(padding, 0, padding, 0);

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            title.setCompoundDrawablesWithIntrinsicBounds(
                    DrawableHelper.getTintedDrawable(mContext,
                            R.drawable.ic_toolbar_premium_request, color),
                    null, null, null);

            int primary = ColorHelper.getAttributeColor(mContext, R.attr.colorPrimary);
            int accent = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);
            button.setTextColor(ColorHelper.getTitleTextColor(primary));

            progress.getProgressDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);

            button.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.buy) {
                RequestListener listener = (RequestListener) mContext;
                listener.onBuyPremiumRequest();
            }
        }
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final TextView title;
        private final TextView content;
        private final ImageView icon;
        private final AppCompatCheckBox checkbox;
        private final LinearLayout container;
        private final View divider;

        ContentViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.name);
            content = (TextView) itemView.findViewById(R.id.requested);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            checkbox = (AppCompatCheckBox) itemView.findViewById(R.id.checkbox);
            container = (LinearLayout) itemView.findViewById(R.id.container);
            divider = itemView.findViewById(R.id.divider);

            CardView card = (CardView) itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getRequestStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT &&
                    card != null) {
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
                if (card != null) card.setCardElevation(0);
            }

            container.setOnClickListener(this);
            container.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                int position = mShowPremiumRequest ?
                        getAdapterPosition() - 1 : getAdapterPosition();
                if (toggleSelection(position)) {
                    checkbox.toggle();
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                int position = mShowPremiumRequest ?
                        getAdapterPosition() - 1 : getAdapterPosition();
                if (toggleSelection(position)) {
                    checkbox.toggle();
                    return true;
                }
            }
            return false;
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(View itemView) {
            super(itemView);
            View shadow = itemView.findViewById(R.id.shadow);
            if (!Preferences.get(mContext).isShadowEnabled()) {
                shadow.setVisibility(View.GONE);
            }
        }
    }

    @Nullable
    private StaggeredGridLayoutManager.LayoutParams getLayoutParams(@Nullable View view) {
        if (view != null) {
            try {
                return (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            } catch (Exception e) {
                LogUtil.d(Log.getStackTraceString(e));
            }
        }
        return null;
    }

    private boolean toggleSelection(int position) {
        if (position >= 0 && position < mRequests.size()) {
            if (mSelectedItems.get(position, false))
                mSelectedItems.delete(position);
            else mSelectedItems.put(position, true);
            try {
                RequestListener listener = (RequestListener) mContext;
                listener.onRequestSelected(getSelectedItemsSize());
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    public boolean selectAll() {
        if (mSelectedAll) {
            mSelectedAll = false;
            resetSelectedItems();
            return false;
        }

        mSelectedItems.clear();
        for (int i = 0; i < mRequests.size(); i++) {
            if (!mRequests.get(i).isRequested())
                mSelectedItems.put(i, true);
        }
        mSelectedAll = mSelectedItems.size() > 0;
        notifyDataSetChanged();
        try {
            RequestListener listener = (RequestListener) mContext;
            listener.onRequestSelected(getSelectedItemsSize());
        } catch (Exception ignored) {}
        return mSelectedAll;
    }

    public void setRequested(int position, boolean requested) {
        mRequests.get(position).setRequested(requested);
    }

    public int getSelectedItemsSize() {
        return mSelectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < mSelectedItems.size(); i++) {
            selected.add(mSelectedItems.keyAt(i));
        }
        return selected;
    }

    public SparseBooleanArray getSelectedItemsArray() {
        return mSelectedItems;
    }

    public void setSelectedItemsArray(SparseBooleanArray selectedItems) {
        mSelectedItems = selectedItems;
        notifyDataSetChanged();
    }

    public void resetSelectedItems() {
        mSelectedAll = false;
        mSelectedItems.clear();
        try {
            RequestListener listener = (RequestListener) mContext;
            listener.onRequestSelected(getSelectedItemsSize());
        } catch (Exception ignored) {}
        notifyDataSetChanged();
    }

    public Request getRequest(int position) {
        return mRequests.get(position);
    }

    public List<Request> getSelectedApps() {
        List<Request> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            int position = mSelectedItems.keyAt(i);
            if (position >= 0 && position < mRequests.size()) {
                Request request = mRequests.get(mSelectedItems.keyAt(i));
                items.add(request);
            }
        }
        return items;
    }

    public boolean isContainsRequested() {
        List<Request> requests = getSelectedApps();
        boolean requested = false;
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).isRequested()) {
                requested = true;
                break;
            }
        }
        return requested;
    }
}
