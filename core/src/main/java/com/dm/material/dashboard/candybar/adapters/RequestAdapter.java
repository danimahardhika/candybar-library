package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.AppCompatCheckBox;
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
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;

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
    private final SparseBooleanArray mSelectedItems;
    private final SparseArrayCompat<Request> mRequests;

    private final int mTextColorSecondary;
    private final int mTextColorAccent;

    public RequestAdapter(@NonNull Context context, @NonNull SparseArrayCompat<Request> requests) {
        mContext = context;
        mRequests = requests;
        mTextColorSecondary = ColorHelper.getAttributeColor(mContext,
                android.R.attr.textColorSecondary);
        mTextColorAccent = ColorHelper.getAttributeColor(mContext, R.attr.colorAccent);
        mSelectedItems = new SparseBooleanArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_request_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        Bitmap bitmap = ((BitmapDrawable) holder.icon.getDrawable()).getBitmap();
        if (bitmap != null) bitmap.recycle();

        holder.requested.setTextColor(mTextColorSecondary);
        holder.icon.setImageBitmap(null);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.icon.setImageBitmap(DrawableHelper.getBitmap(
                mRequests.get(position).getIcon(), true));

        holder.name.setText(mRequests.get(position).getName());
        holder.activity.setText(mRequests.get(position).getActivity());

        if (mRequests.get(position).isRequested()) {
            holder.requested.setTextColor(mTextColorAccent);
            holder.requested.setText(mContext.getResources().getString(
                    R.string.request_already_requested));
        } else {
            holder.requested.setText(mContext.getResources().getString(
                    R.string.request_not_requested));
        }

        holder.checkbox.setChecked(mSelectedItems.get(position, false));
    }

    @Override
    public int getItemCount() {
        return mRequests.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        final TextView name;
        final TextView activity;
        final TextView requested;
        final ImageView icon;
        final AppCompatCheckBox checkbox;
        final LinearLayout container;

        ViewHolder(View itemView) {
            super(itemView);
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
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                if (toggleSelection(getAdapterPosition())) {
                    checkbox.toggle();
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                if (toggleSelection(getAdapterPosition())) {
                    checkbox.toggle();
                    return true;
                }
            }
            return false;
        }
    }

    public void addRequest(Request request) {
        mRequests.append(mRequests.size(), request);
        notifyItemInserted(getItemCount() - 1);
    }

    private boolean toggleSelection(int position) {
        if (position >= 0 && position < mRequests.size()) {
            if (mSelectedItems.get(position, false))
                mSelectedItems.delete(position);
            else mSelectedItems.put(position, true);
            try {
                RequestListener listener = (RequestListener) mContext;
                listener.OnSelected(getSelectedItemsSize());
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    public void selectAll() {
        if (mSelectedItems.size() == mRequests.size()) {
            resetSelectedItems();
            return;
        }

        mSelectedItems.clear();
        for (int i = 0; i < mRequests.size(); i++) {
            mSelectedItems.put(i, true);
        }
        notifyDataSetChanged();
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

    public SparseArrayCompat<Integer> getSelectedItems() {
        SparseArrayCompat<Integer> selected = new SparseArrayCompat<>();
        for (int i = 0; i < mSelectedItems.size(); i++) {
            selected.append(selected.size(), mSelectedItems.keyAt(i));
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

    public Request getRequest(int position) {
        return mRequests.get(position);
    }

    private SparseArrayCompat<Request> getSelectedApps() {
        SparseArrayCompat<Request> items = new SparseArrayCompat<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            int position = mSelectedItems.keyAt(i);
            if (position >= 0 && position < mRequests.size()) {
                Request request = mRequests.get(mSelectedItems.keyAt(i));
                items.append(items.size(), request);
            }
        }
        return items;
    }

    public boolean isContainsRequested() {
        SparseArrayCompat<Request> requests = getSelectedApps();
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
