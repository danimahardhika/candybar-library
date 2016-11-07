package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;

import java.util.ArrayList;
import java.util.List;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
 *
 * Licensed under the Apache LicenseHelper, Version 2.0 (the "LicenseHelper");
 * you may not use this file except in compliance with the LicenseHelper.
 * You may obtain a copy of the LicenseHelper at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the LicenseHelper is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the LicenseHelper for the specific language governing permissions and
 * limitations under the LicenseHelper.
 */

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private Context mContext;
    private List<Request> mRequests;
    private SparseBooleanArray mSelectedItems;

    private int mTextColorSecondary;
    private int mTextColorAccent;

    private static final int TYPE_PREMIUM = 0;
    private static final int TYPE_REGULAR = 1;

    public RequestAdapter(@NonNull Context context, @NonNull List<Request> requests) {
        mContext = context;
        mRequests = requests;
        mTextColorSecondary = ColorHelper.getAttributeColor(mContext,
                android.R.attr.textColorSecondary);
        mTextColorAccent = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);
        mSelectedItems = new SparseBooleanArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_PREMIUM) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_request_premium_item_list, parent, false);
        } else if (viewType == TYPE_REGULAR) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_request_item_list, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.holderId == TYPE_REGULAR) {
            holder.requested.setTextColor(mTextColorSecondary);
            holder.icon.setImageBitmap(null);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.holderId == TYPE_PREMIUM) {
            boolean isPremiumRequestEnabled = mContext.getResources().getBoolean(
                    R.bool.enable_premium_request);
            if (!isPremiumRequestEnabled) holder.premiumRequest.setVisibility(View.GONE);
            else {
                if (Preferences.getPreferences(mContext).isPremiumRequest()) {
                    String count = mContext.getResources().getString(R.string.premium_request_count)
                            +" "+ Preferences.getPreferences(mContext).getPremiumRequestCount();
                    holder.count.setText(count);
                    holder.count.setVisibility(View.VISIBLE);
                } else holder.count.setVisibility(View.GONE);
            }
        } else if (holder.holderId == TYPE_REGULAR) {
            int finalPosition = position - 1;

            holder.icon.setImageBitmap(mRequests.get(finalPosition).getIcon());

            holder.name.setText(mRequests.get(finalPosition).getName());
            holder.activity.setText(mRequests.get(finalPosition).getActivity());

            if (mRequests.get(finalPosition).isRequested()) {
                holder.requested.setTextColor(mTextColorAccent);
                holder.requested.setText(mContext.getResources().getString(
                        R.string.request_already_requested));
            } else {
                holder.requested.setText(mContext.getResources().getString(
                        R.string.request_not_requested));
            }

            holder.checkbox.setChecked(mSelectedItems.get(finalPosition, false));
        }
    }

    @Override
    public int getItemCount() {
        return mRequests.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_PREMIUM : TYPE_REGULAR;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        CardView premiumRequest;
        TextView buyPackage;
        TextView count;
        TextView name;
        TextView activity;
        TextView requested;
        ImageView icon;
        AppCompatCheckBox checkbox;
        LinearLayout container;

        int holderId;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == TYPE_PREMIUM) {
                premiumRequest = (CardView) itemView.findViewById(R.id.premium_request);
                buyPackage = (TextView) itemView.findViewById(R.id.buy_package);
                count = (TextView) itemView.findViewById(R.id.count);
                buyPackage.setOnClickListener(this);
                buyPackage.setBackgroundResource(Preferences.getPreferences(mContext).isDarkTheme() ?
                        R.drawable.button_accent_dark : R.drawable.button_accent);

                holderId = TYPE_PREMIUM;
            } else if (viewType == TYPE_REGULAR) {
                name = (TextView) itemView.findViewById(R.id.name);
                activity = (TextView) itemView.findViewById(R.id.activity);
                requested = (TextView) itemView.findViewById(R.id.requested);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                checkbox = (AppCompatCheckBox) itemView.findViewById(R.id.checkbox);
                container = (LinearLayout) itemView.findViewById(R.id.container);
                container.setBackgroundResource(Preferences.getPreferences(mContext).isDarkTheme() ?
                        R.drawable.card_item_list_dark : R.drawable.card_item_list);
                container.setOnClickListener(this);
                container.setOnLongClickListener(this);

                holderId = TYPE_REGULAR;
            }
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                int position = getAdapterPosition();
                toggleSelection(position);
            } else if (id == R.id.buy_package) {
                try {
                    RequestListener listener = (RequestListener) mContext;
                    listener.OnBuyPremiumRequest();
                } catch (Exception ignored) {}
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                int position = getAdapterPosition();
                toggleSelection(position);
                return true;
            }
            return false;
        }
    }

    private void toggleSelection(int position) {
        int finalPosition = position - 1;
        if (mSelectedItems.get(finalPosition, false))
            mSelectedItems.delete(finalPosition);
        else mSelectedItems.put(finalPosition, true);
        notifyItemChanged(position);
        try {
            RequestListener listener = (RequestListener) mContext;
            listener.OnSelected(getSelectedItemsSize());
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

    public void resetSelectedItems() {
        mSelectedItems.clear();
        try {
            RequestListener listener = (RequestListener) mContext;
            listener.OnSelected(getSelectedItemsSize());
        } catch (Exception ignored) {}
        notifyDataSetChanged();
    }

    public void resetAdapter() {
        notifyItemChanged(0);
        resetSelectedItems();
    }

    public Request getRequest(int position) {
        return mRequests.get(position);
    }

    private List<Request> getSelectedApps() {
        List<Request> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            Request request = mRequests.get(mSelectedItems.keyAt(i));
            items.add(request);
        }
        return items;
    }

    public boolean isContainsRequested() {
        List<Request> requests = getSelectedApps();
        boolean requested = false;
        for (Request request : requests) {
            if (request.isRequested()) {
                requested = true;
                break;
            }
        }
        return requested;
    }

}
