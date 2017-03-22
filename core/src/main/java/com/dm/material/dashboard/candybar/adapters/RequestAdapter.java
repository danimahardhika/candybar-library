package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
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

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
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

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Request> mRequests;
    private SparseBooleanArray mSelectedItems;
    private final DisplayImageOptions.Builder mOptions;

    private final int mTextColorPrimary;
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
        mTextColorPrimary = ColorHelper.getAttributeColor(mContext,
                android.R.attr.textColorPrimary);
        mTextColorAccent = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);
        mSelectedItems = new SparseBooleanArray();

        mShowShadow = (spanCount == 1);
        mShowPremiumRequest = Preferences.getPreferences(mContext).isPremiumRequestEnabled();

        mOptions = ImageConfig.getRawDefaultImageOptions();
        mOptions.resetViewBeforeLoading(true);
        mOptions.cacheInMemory(true);
        mOptions.cacheOnDisk(false);
        mOptions.showImageOnFail(R.drawable.ic_app_default);
        mOptions.displayer(new FadeInBitmapDisplayer(700));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_request_item_header, parent, false);
        } else if (viewType == TYPE_CONTENT) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_request_item_list, parent, false);
        } else if (viewType == TYPE_FOOTER) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_request_item_footer, parent, false);
        }

        try {
            if (view != null) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams)
                        view.getLayoutParams();

                if (viewType == TYPE_FOOTER) {
                    layoutParams.setFullSpan(true);
                } else {
                    layoutParams.setFullSpan(false);
                }
            }
        } catch (Exception e) {
            LogUtil.d(Log.getStackTraceString(e));
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.holderId == TYPE_CONTENT) {
            holder.content.setTextColor(mTextColorPrimary);

            if (mShowShadow) {
                holder.divider.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.holderId == TYPE_HEADER) {
            if (Preferences.getPreferences(mContext).isPremiumRequest()) {
                holder.button.setVisibility(View.GONE);
                holder.content.setVisibility(View.GONE);
                holder.container.setVisibility(View.VISIBLE);

                int total = Preferences.getPreferences(mContext).getPremiumRequestTotal();
                int available = Preferences.getPreferences(mContext).getPremiumRequestCount();

                holder.total.setText(String.format(
                        mContext.getResources().getString(R.string.premium_request_count),
                        total));
                holder.available.setText(String.format(
                        mContext.getResources().getString(R.string.premium_request_available),
                        available));
                holder.used.setText(String.format(
                        mContext.getResources().getString(R.string.premium_request_used),
                        (total - available)));

                holder.progress.setMax(total);
                holder.progress.setProgress(available);
            } else {
                holder.button.setVisibility(View.VISIBLE);
                holder.content.setVisibility(View.VISIBLE);
                holder.container.setVisibility(View.GONE);
            }
        } else if (holder.holderId == TYPE_CONTENT) {
            int finalPosition = mShowPremiumRequest ? position - 1 : position;

            ImageLoader.getInstance().displayImage(mRequests.get(finalPosition).getPackageName(),
                    new ImageViewAware(holder.icon), mOptions.build(),
                    new ImageSize(114, 114), null, null);

            holder.title.setText(mRequests.get(finalPosition).getName());
            holder.subtitle.setText(mRequests.get(finalPosition).getActivity());

            if (mRequests.get(finalPosition).isRequested()) {
                holder.content.setTextColor(mTextColorAccent);
                holder.content.setText(mContext.getResources().getString(
                        R.string.request_already_requested));
            } else {
                holder.content.setText(mContext.getResources().getString(
                        R.string.request_not_requested));
            }

            holder.checkbox.setChecked(mSelectedItems.get(finalPosition, false));

            if (finalPosition == (mRequests.size() - 1) && mShowShadow) {
                holder.divider.setVisibility(View.GONE);
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        private TextView title;
        private TextView subtitle;
        private TextView content;
        private TextView total;
        private TextView available;
        private TextView used;
        private ImageView icon;
        private AppCompatCheckBox checkbox;
        private AppCompatButton button;
        private LinearLayout container;
        private View divider;
        private ProgressBar progress;

        private int holderId;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == TYPE_HEADER) {
                title = (TextView) itemView.findViewById(R.id.title);
                content = (TextView) itemView.findViewById(R.id.content);
                button = (AppCompatButton) itemView.findViewById(R.id.buy);

                container = (LinearLayout) itemView.findViewById(R.id.premium_request);
                total = (TextView) itemView.findViewById(R.id.premium_request_total);
                available = (TextView) itemView.findViewById(R.id.premium_request_available);
                used = (TextView) itemView.findViewById(R.id.premium_request_used);
                progress = (ProgressBar) itemView.findViewById(R.id.progress);
                holderId = TYPE_HEADER;

                int padding = mContext.getResources().getDimensionPixelSize(R.dimen.content_margin) +
                        mContext.getResources().getDimensionPixelSize(R.dimen.icon_size_small);
                content.setPadding(padding, 0, 0, 0);
                container.setPadding(padding, 0, padding, 0);

                int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                title.setCompoundDrawablesWithIntrinsicBounds(
                        DrawableHelper.getTintedDrawable(mContext,
                                R.drawable.ic_toolbar_premium_request, color),
                        null, null, null);

                int accent = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);
                button.setTextColor(ColorHelper.getTitleTextColor(accent));

                progress.getProgressDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);

                button.setOnClickListener(this);
            } else if (viewType == TYPE_CONTENT) {
                title = (TextView) itemView.findViewById(R.id.name);
                subtitle = (TextView) itemView.findViewById(R.id.activity);
                content = (TextView) itemView.findViewById(R.id.requested);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                checkbox = (AppCompatCheckBox) itemView.findViewById(R.id.checkbox);
                container = (LinearLayout) itemView.findViewById(R.id.container);
                divider = itemView.findViewById(R.id.divider);
                holderId = TYPE_CONTENT;

                container.setOnClickListener(this);
                container.setOnLongClickListener(this);
            } else if (viewType == TYPE_FOOTER) {
                holderId = TYPE_FOOTER;
            }
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
            } else if (id == R.id.buy) {
                RequestListener listener = (RequestListener) mContext;
                listener.onBuyPremiumRequest();
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

    public void selectAll() {
        if (mSelectedAll) {
            mSelectedAll = false;
            resetSelectedItems();
            return;
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

    private List<Request> getSelectedApps() {
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
