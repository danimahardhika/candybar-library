package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.LauncherHelper;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

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

public class LauncherAdapter extends RecyclerView.Adapter<LauncherAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Icon> mLaunchers;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT = 1;

    public LauncherAdapter(@NonNull Context context, @NonNull List<Icon> launchers) {
        mContext = context;
        mLaunchers = launchers;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_apply_item_header, parent, false);
        } else if (viewType == TYPE_CONTENT) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_apply_item_grid, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.holderId == TYPE_HEADER) {
            holder.name.setText(mLaunchers.get(position).getTitle());
        } else if (holder.holderId == TYPE_CONTENT) {
            holder.name.setText(mLaunchers.get(position).getTitle());
            ImageLoader.getInstance().displayImage("drawable://" +mLaunchers.get(position).getRes(),
                    holder.icon, ImageConfig.getDefaultImageOptions(false),
                    new SimpleImageLoadingListener() {

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            super.onLoadingStarted(imageUri, view);
                            int color = ColorHelper.getAttributeColor(
                                    mContext, R.attr.card_background);
                            holder.name.setBackgroundColor(color);
                            holder.name.setTextColor(ColorHelper.getTitleTextColor(color));
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            Palette.from(loadedImage).generate(palette -> {
                                int defaultColor = ColorHelper.getAttributeColor(
                                        mContext, R.attr.card_background);
                                int color = palette.getVibrantColor(defaultColor);
                                if (color == defaultColor)
                                    color = palette.getMutedColor(defaultColor);
                                holder.name.setBackgroundColor(
                                        ColorHelper.getDarkerColor(color, 0.8f));
                                int text = ColorHelper.getTitleTextColor(
                                        ColorHelper.getDarkerColor(color, 0.8f));
                                holder.name.setTextColor(text);
                            });
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return mLaunchers.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getFirstHeaderPosition() || position == getLastHeaderPosition())
            return TYPE_HEADER;
        return TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView name;
        private ImageView icon;
        private LinearLayout container;

        private int holderId;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == TYPE_HEADER) {
                name = (TextView) itemView.findViewById(R.id.name);
                holderId = TYPE_HEADER;
            } else if (viewType == TYPE_CONTENT) {
                icon = (ImageView) itemView.findViewById(R.id.icon);
                name = (TextView) itemView.findViewById(R.id.name);
                container = (LinearLayout) itemView.findViewById(R.id.container);
                container.setOnClickListener(this);

                holderId = TYPE_CONTENT;
            }
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (id == R.id.container) {
                if (position < 0 || position > getItemCount()) return;
                try {
                    LauncherHelper.apply(mContext,
                            mLaunchers.get(position).getPackageName(),
                            mLaunchers.get(position).getTitle());
                } catch (Exception e) {
                    Toast.makeText(mContext, String.format(mContext.getResources().getString(
                            R.string.apply_launch_failed), mLaunchers.get(position).getTitle()),
                            Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    public int getFirstHeaderPosition() {
        return mLaunchers.indexOf(new Icon(
                mContext.getResources().getString(R.string.apply_installed), -1, null));
    }

    public int getLastHeaderPosition() {
        return mLaunchers.indexOf(new Icon(
                mContext.getResources().getString(R.string.apply_supported), -2, null));
    }
}
