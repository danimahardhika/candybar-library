package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;

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

public class ChangelogAdapter extends BaseAdapter {

    private Context mContext;
    private String[] mChangelog;

    public ChangelogAdapter(@NonNull Context context, @NonNull String[] changelog) {
        mContext = context;
        mChangelog = changelog;
    }

    @Override
    public int getCount() {
        return mChangelog.length;
    }

    @Override
    public String getItem(int position) {
        return mChangelog[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_changelog_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.changelog.setText(Html.fromHtml(mChangelog[position],
                    Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.changelog.setText(Html.fromHtml(mChangelog[position]));
        }
        return view;
    }

    private class ViewHolder {

        TextView changelog;
        ImageView dot;

        ViewHolder(View view) {
            changelog = (TextView) view.findViewById(R.id.changelog);
            dot = (ImageView) view.findViewById(R.id.dot);
            int color = ColorHelper.getAttributeColor(mContext,
                    android.R.attr.textColorSecondary);
            dot.setImageDrawable(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_changelog_dot, color));
        }

    }

}
