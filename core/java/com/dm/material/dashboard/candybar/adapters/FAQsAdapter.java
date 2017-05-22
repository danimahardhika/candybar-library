package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.items.FAQs;
import com.dm.material.dashboard.candybar.preferences.Preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


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

public class FAQsAdapter extends RecyclerView.Adapter<FAQsAdapter.ViewHolder> {

    private final Context mContext;
    private final List<FAQs> mFAQs;
    private final List<FAQs> mFAQsAll;

    private static final int TYPE_CONTENT = 0;
    private static final int TYPE_FOOTER = 1;

    public FAQsAdapter(@NonNull Context context, @NonNull List<FAQs> faqs) {
        mContext = context;
        mFAQs = faqs;
        mFAQsAll = new ArrayList<>();
        mFAQsAll.addAll(mFAQs);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == TYPE_CONTENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.fragment_faqs_item_list, parent, false);
        } else if (viewType == TYPE_FOOTER) {
            view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.fragment_settings_item_footer, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.holderId == TYPE_CONTENT) {
            holder.divider.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.holderId == TYPE_CONTENT) {
            holder.question.setText(mFAQs.get(position).getQuestion());
            holder.answer.setText(mFAQs.get(position).getAnswer());

            if (position == (mFAQs.size() - 1)) {
                holder.divider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFAQs.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) return TYPE_FOOTER;
        return TYPE_CONTENT;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView question;
        private TextView answer;
        private View divider;

        private int holderId;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == TYPE_CONTENT) {
                question = (TextView) itemView.findViewById(R.id.question);
                answer = (TextView) itemView.findViewById(R.id.answer);
                divider = itemView.findViewById(R.id.divider);
                holderId = TYPE_CONTENT;
            } else if (viewType == TYPE_FOOTER) {
                holderId = TYPE_FOOTER;

                if (!Preferences.get(mContext).isShadowEnabled()) {
                    View shadow = itemView.findViewById(R.id.shadow);
                    shadow.setVisibility(View.GONE);
                }
            }
        }
    }

    public void search(String string) {
        String query = string.toLowerCase(Locale.getDefault()).trim();
        mFAQs.clear();
        if (query.length() == 0) mFAQs.addAll(mFAQsAll);
        else {
            for (int i = 0; i < mFAQsAll.size(); i++) {
                FAQs faq = mFAQsAll.get(i);
                String question = faq.getQuestion().toLowerCase(Locale.getDefault());
                String answer = faq.getAnswer().toLowerCase(Locale.getDefault());
                if (question.contains(query) || answer.contains(query)) {
                    mFAQs.add(faq);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getFaqsCount() {
        return mFAQs.size();
    }
}
