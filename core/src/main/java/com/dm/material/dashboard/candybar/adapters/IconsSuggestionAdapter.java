package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.IntentHelper;
import com.dm.material.dashboard.candybar.helpers.SoftKeyboardHelper;
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

public class IconsSuggestionAdapter extends ArrayAdapter<Icon> {

    private Context mContext;
    private List<Icon> mIcons;
    private List<Icon> mIconsAll;

    private int mResource;

    public IconsSuggestionAdapter(@NonNull Context context, int resource, @NonNull List<Icon> icons) {
        super(context, resource, icons);
        mContext = context;
        mResource = resource;
        mIcons = icons;
        mIconsAll = icons;
    }

    @Override
    public int getCount() {
        return mIcons.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public Icon getItem(int position) {
        return mIcons.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ViewHolder {

        ImageView icon;
        TextView name;
        TextView section;
        View separator;
        LinearLayout container;

        ViewHolder(View view) {
            container = (LinearLayout) view.findViewById(R.id.container);
            name = (TextView) view.findViewById(R.id.name);
            section = (TextView) view.findViewById(R.id.section);
            icon = (ImageView) view.findViewById(R.id.icon);
            separator = view.findViewById(R.id.separator);
            container.setBackgroundResource(Preferences.getPreferences(mContext).isDarkTheme() ?
                    R.drawable.item_grid_dark : R.drawable.item_grid);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            holder.separator.setVisibility(View.VISIBLE);
        }

        holder.name.setText(mIcons.get(position).getTitle());
        holder.section.setText(mIcons.get(position).getSection());
        ImageLoader.getInstance().displayImage("drawable://" + mIcons.get(position).getRes(),
                holder.icon, ImageConfig.getIconOptions());
        if (position == 0) holder.separator.setVisibility(View.GONE);

        holder.container.setOnClickListener(v -> {
            IconsHelper.selectIcon(mContext, IntentHelper.sAction, mIcons.get(position));
            SoftKeyboardHelper.closeKeyboard(mContext);
        });

        return view;
    }

    private Filter mFilter = new Filter() {

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Icon) resultValue).getTitle();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            List<Icon> suggestion = new ArrayList<>();
            if (constraint != null) {
                constraint = constraint.toString().toLowerCase(Locale.getDefault());
                for (Icon icon : mIconsAll) {
                    String title = icon.getTitle().toLowerCase(Locale.getDefault());
                    if (title.contains(constraint)) {
                        suggestion.add(icon);
                    }
                }
            }
            result.values = suggestion;
            result.count = suggestion.size();
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mIcons = (List<Icon>) results.values;
            notifyDataSetChanged();
        }

    };

}
