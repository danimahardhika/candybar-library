package com.dm.material.dashboard.candybar.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.items.Credit;
import com.dm.material.dashboard.candybar.utils.LogUtil;

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

public class CreditsAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<Credit> mCredits;

    public CreditsAdapter(@NonNull Context context, @NonNull List<Credit> credits) {
        mContext = context;
        mCredits = credits;
    }

    @Override
    public int getCount() {
        return mCredits.size();
    }

    @Override
    public Credit getItem(int position) {
        return mCredits.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_credits_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.title.setText(mCredits.get(position).getName());
        holder.subtitle.setText(mCredits.get(position).getContribution());
        holder.container.setOnClickListener(view1 -> {
            String link = mCredits.get(position).getLink();
            if (URLUtil.isValidUrl(link)) {
                try {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                } catch (ActivityNotFoundException e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            }
        });
        return view;
    }

    class ViewHolder {

        LinearLayout container;
        TextView title;
        TextView subtitle;

        ViewHolder(View view) {
            container = (LinearLayout) view.findViewById(R.id.container);
            title = (TextView) view.findViewById(R.id.title);
            subtitle = (TextView) view.findViewById(R.id.subtitle);
        }
    }
}
