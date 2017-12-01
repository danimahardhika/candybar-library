package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.fragments.dialog.LanguagesFragment;
import com.dm.material.dashboard.candybar.items.Language;

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

public class LanguagesAdapter extends BaseAdapter {

    private Context mContext;
    private List<Language> mLanguages;
    private int mSelectedIndex;

    public LanguagesAdapter(@NonNull Context context, @NonNull List<Language> languages, int selectedIndex) {
        mContext = context;
        mLanguages = languages;
        mSelectedIndex = selectedIndex;
    }

    @Override
    public int getCount() {
        return mLanguages.size();
    }

    @Override
    public Language getItem(int position) {
        return mLanguages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LanguagesAdapter.ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_inapp_dialog_item_list, null);
            holder = new LanguagesAdapter.ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (LanguagesAdapter.ViewHolder) view.getTag();
        }

        holder.radio.setChecked(mSelectedIndex == position);
        holder.name.setText(mLanguages.get(position).getName());

        holder.container.setOnClickListener(v -> {
            FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
            if (fm == null) return;

            Fragment fragment = fm.findFragmentByTag(LanguagesFragment.TAG);
            if (fragment == null) return;

            if (fragment instanceof LanguagesFragment) {
                ((LanguagesFragment) fragment).setLanguage(mLanguages.get(position).getLocale());
            }
        });
        return view;
    }

    private class ViewHolder {

        private final AppCompatRadioButton radio;
        private final TextView name;
        private final LinearLayout container;

        ViewHolder(View view) {
            radio = view.findViewById(R.id.radio);
            name = view.findViewById(R.id.name);
            container = view.findViewById(R.id.container);
        }
    }
}
