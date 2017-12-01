package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.dm.material.dashboard.candybar.R;

import org.sufficientlysecure.htmltextview.HtmlTextView;

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

public class ChangelogAdapter extends BaseAdapter {

    private final Context mContext;
    private final String[] mChangelog;

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

        holder.changelog.setHtml(mChangelog[position]);
        return view;
    }

    private class ViewHolder {

        private final HtmlTextView changelog;

        ViewHolder(View view) {
            changelog = view.findViewById(R.id.changelog);
            int color = ColorHelper.getAttributeColor(mContext,
                    R.attr.colorAccent);
            changelog.setCompoundDrawablesWithIntrinsicBounds(
                    DrawableHelper.getTintedDrawable(mContext, R.drawable.ic_changelog_dot, color),
                    null, null, null);
        }
    }
}
