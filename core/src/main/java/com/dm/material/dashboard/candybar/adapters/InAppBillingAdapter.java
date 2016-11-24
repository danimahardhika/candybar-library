package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.items.InAppBilling;
import com.dm.material.dashboard.candybar.preferences.Preferences;

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

public class InAppBillingAdapter extends BaseAdapter {

    private Context mContext;
    private InAppBilling[] mInAppBillings;

    private int mSelectedPosition = 0;

    public InAppBillingAdapter(@NonNull Context context, @NonNull InAppBilling[] inAppBillings) {
        mContext = context;
        mInAppBillings = inAppBillings;
    }

    @Override
    public int getCount() {
        return mInAppBillings.length;
    }

    @Override
    public InAppBilling getItem(int position) {
        return mInAppBillings[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_inapp_dialog_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.radio.setChecked(mSelectedPosition == position);

        String product = mInAppBillings[position].getPrice() +" - "+
                mInAppBillings[position].getProductName();
        holder.name.setText(product);

        holder.container.setOnClickListener(v -> {
            mSelectedPosition = position;
            notifyDataSetChanged();
        });

        return view;
    }

    private class ViewHolder {

        AppCompatRadioButton radio;
        TextView name;
        LinearLayout container;

        ViewHolder(View view) {
            radio = (AppCompatRadioButton) view.findViewById(R.id.radio);
            name = (TextView) view.findViewById(R.id.name);
            container = (LinearLayout) view.findViewById(R.id.container);
            container.setBackgroundResource(Preferences.getPreferences(mContext).isDarkTheme() ?
                    R.drawable.card_item_list_dark : R.drawable.card_item_list);
        }
    }

    public InAppBilling getSelectedProduct() {
        return mInAppBillings[mSelectedPosition];
    }

}
