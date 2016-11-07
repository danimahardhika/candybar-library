package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.items.Feature;

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

public class HomeFeaturesAdapter extends RecyclerView.Adapter<HomeFeaturesAdapter.ViewHolder> {

    private Context mContext;
    private List<Feature> mFeatures;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT = 1;

    public HomeFeaturesAdapter(@NonNull Context context, @NonNull List<Feature> features) {
        mContext = context;
        mFeatures = features;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_feature_header, parent, false);
        } else  if (viewType == TYPE_CONTENT) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_feature_item_list, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.holderId == TYPE_CONTENT) {
            int finalPosition = position - 1;
            if (mFeatures.get(finalPosition).getTitle().length() > 0) {
                holder.container.setVisibility(View.VISIBLE);
                Drawable icon = DrawableHelper.getTintedDrawable(
                        mContext, mFeatures.get(finalPosition).getIcon(),
                        ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary));
                holder.icon.setImageDrawable(icon);
                holder.title.setText(mFeatures.get(finalPosition).getTitle());
            } else holder.container.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mFeatures.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        ImageView icon;
        TextView title;

        int holderId;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == TYPE_HEADER) {
                holderId = TYPE_HEADER;
            } else if (viewType == TYPE_CONTENT) {
                container = (LinearLayout) itemView.findViewById(R.id.container);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                title = (TextView) itemView.findViewById(R.id.title);

                holderId = TYPE_CONTENT;
            }
        }
    }

}
