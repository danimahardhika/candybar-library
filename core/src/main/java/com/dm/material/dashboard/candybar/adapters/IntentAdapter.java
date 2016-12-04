package com.dm.material.dashboard.candybar.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.fragments.dialog.IntentChooserFragment;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.FileHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;

import java.io.File;
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

public class IntentAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<ResolveInfo> mApps;
    private final Request mRequest;

    public IntentAdapter(@NonNull Context context, @NonNull List<ResolveInfo> apps,
                         @NonNull Request request) {
        mContext = context;
        mApps = apps;
        mRequest = request;
    }

    @Override
    public int getCount() {
        return mApps.size();
    }

    @Override
    public ResolveInfo getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_intent_chooser_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            holder.divider.setVisibility(View.VISIBLE);
        }

        holder.icon.setImageDrawable(DrawableHelper.getAppIcon(mContext, mApps.get(position)));
        holder.name.setText(mApps.get(position).loadLabel(mContext.getPackageManager()).toString());

        if (position == mApps.size()-1) {
            holder.divider.setVisibility(View.GONE);
        }

        holder.container.setOnClickListener(v -> {
            ActivityInfo app = mApps.get(position).activityInfo;
            ComponentName name = new ComponentName(app.applicationInfo.packageName, app.name);
            sendRequest(name);

            FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
            if (fm != null) {
                DialogFragment dialog = (DialogFragment) fm.findFragmentByTag(
                        IntentChooserFragment.TAG);
                if (dialog!= null) {
                    dialog.dismiss();
                }
            }
        });

        return view;
    }

    private class ViewHolder {

        final TextView name;
        final ImageView icon;
        final LinearLayout container;
        final View divider;

        ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.name);
            icon = (ImageView) view.findViewById(R.id.icon);
            container = (LinearLayout) view.findViewById(R.id.container);
            divider = view.findViewById(R.id.divider);
            container.setBackgroundResource(Preferences.getPreferences(mContext).isDarkTheme() ?
                    R.drawable.card_item_list_dark : R.drawable.card_item_list);
        }
    }

    private void sendRequest(ComponentName name) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");

        if (mRequest.getStream().length() > 0) {
            File zip = new File(mRequest.getStream());
            Uri uri = FileHelper.getUriFromFile(mContext, mContext.getPackageName(), zip);
            if (uri == null) uri = Uri.fromFile(zip);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        intent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{mContext.getResources().getString(R.string.dev_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, mRequest.getSubject());
        intent.putExtra(Intent.EXTRA_TEXT, mRequest.getText());
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setComponent(name);
        mContext.startActivity(intent);
    }

}
