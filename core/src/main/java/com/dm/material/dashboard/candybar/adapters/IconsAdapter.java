package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.IntentHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
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

public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.ViewHolder> {

    private Context mContext;
    private List<Icon> mIcons;
    private List<Icon> mIconsAll;
    private boolean mIsShowIconName;

    public IconsAdapter(@NonNull Context context, @NonNull List<Icon> icons) {
        mContext = context;
        mIcons = icons;
        mIconsAll = new ArrayList<>();
        mIsShowIconName = mContext.getResources().getBoolean(R.bool.show_icon_name);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_icons_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mIsShowIconName) {
            holder.name.setVisibility(View.VISIBLE);
            holder.name.setText(mIcons.get(position).getTitle());
        } else {
            holder.name.setVisibility(View.GONE);
        }

        ImageLoader.getInstance().displayImage("drawable://" + mIcons.get(position).getRes(),
                holder.icon, ImageConfig.getIconOptions());
    }

    @Override
    public int getItemCount() {
        return mIcons.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView icon;
        TextView name;
        LinearLayout container;

        ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            name = (TextView) itemView.findViewById(R.id.name);
            container = (LinearLayout) itemView.findViewById(R.id.container);
            container.setOnClickListener(this);
            container.setBackgroundResource(Preferences.getPreferences(mContext).isDarkTheme() ?
                    R.drawable.item_grid_dark : R.drawable.item_grid);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (id == R.id.container) {
                IconsHelper.selectIcon(mContext, IntentHelper.sAction, mIcons.get(position));
            }
        }
    }

    public void initSearch() {
        mIconsAll.clear();
        mIconsAll.addAll(mIcons);
    }

    public void resetSearch() {
        mIconsAll.clear();
    }

    public void search(String query) {
        query = query.toLowerCase(Locale.getDefault());
        mIcons.clear();
        if (query.length() == 0) {
            mIcons.addAll(mIconsAll);
        } else {
            for (Icon icon : mIconsAll) {
                String title = icon.getTitle().toLowerCase(Locale.getDefault());
                if (title.contains(query)) {
                    mIcons.add(icon);
                }
            }
        }
        notifyDataSetChanged();
    }

}
