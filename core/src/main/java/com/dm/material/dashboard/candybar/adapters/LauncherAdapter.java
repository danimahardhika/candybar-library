package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.LauncherHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.nostra13.universalimageloader.core.ImageLoader;

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

public class LauncherAdapter extends RecyclerView.Adapter<LauncherAdapter.ViewHolder> {

    private final Context mContext;
    private final SparseArrayCompat<Icon> mLaunchers;

    public LauncherAdapter(@NonNull Context context, @NonNull SparseArrayCompat<Icon> launchers) {
        mContext = context;
        mLaunchers = launchers;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_apply_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(mLaunchers.get(position).getTitle());
        ImageLoader.getInstance().displayImage("drawable://" +mLaunchers.get(position).getRes(),
                holder.icon, ImageConfig.getDefaultImageOptions(false));
    }

    @Override
    public int getItemCount() {
        return mLaunchers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView name;
        final ImageView icon;
        final LinearLayout container;

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
                if (position < 0 || position > mLaunchers.size()) return;
                try {
                    LauncherHelper.apply(mContext,
                            mLaunchers.get(position).getPackageName(),
                            mLaunchers.get(position).getTitle());
                } catch (SecurityException e) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.unable_launch)
                                    + " " +mLaunchers.get(position).getTitle()+ " " +
                                    mContext.getResources().getString(R.string.unable_launch2),
                            Toast.LENGTH_LONG).show();
                }

            }
        }
    }

}
