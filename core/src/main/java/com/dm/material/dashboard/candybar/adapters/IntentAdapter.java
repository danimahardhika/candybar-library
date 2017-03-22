package com.dm.material.dashboard.candybar.adapters;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.fragments.dialog.IntentChooserFragment;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.FileHelper;
import com.dm.material.dashboard.candybar.items.IntentChooser;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.utils.LogUtil;

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
    private final List<IntentChooser> mApps;
    private final Request mRequest;

    public IntentAdapter(@NonNull Context context, @NonNull List<IntentChooser> apps,
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
    public IntentChooser getItem(int position) {
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

        holder.icon.setImageDrawable(DrawableHelper.getAppIcon(mContext, mApps.get(position).getApp()));
        holder.name.setText(mApps.get(position).getApp().loadLabel(mContext.getPackageManager()).toString());

        if (position == mApps.size()-1) {
            holder.divider.setVisibility(View.GONE);
        }

        if (mApps.get(position).getType() == IntentChooser.TYPE_SUPPORTED) {
            holder.type.setTextColor(ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary));
            holder.type.setText(mContext.getResources().getString(R.string.intent_email_supported));
        } else if (mApps.get(position).getType() == IntentChooser.TYPE_RECOMMENDED) {
            holder.type.setTextColor(ColorHelper.getAttributeColor(mContext, R.attr.colorAccent));
            holder.type.setText(mContext.getResources().getString(R.string.intent_email_recommended));
        } else {
            holder.type.setTextColor(Color.parseColor("#F44336"));
            holder.type.setText(mContext.getResources().getString(R.string.intent_email_not_supported));
        }

        holder.container.setOnClickListener(v -> {
            ActivityInfo app = mApps.get(position).getApp().activityInfo;
            if (mApps.get(position).getType() == IntentChooser.TYPE_RECOMMENDED ||
                    mApps.get(position).getType() == IntentChooser.TYPE_SUPPORTED) {
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
                return;
            }

            Toast.makeText(mContext, R.string.intent_email_not_supported_message,
                    Toast.LENGTH_LONG).show();
        });

        return view;
    }

    private class ViewHolder {

        private final TextView name;
        private final TextView type;
        private final ImageView icon;
        private final LinearLayout container;
        private final View divider;

        ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.name);
            type = (TextView) view.findViewById(R.id.type);
            icon = (ImageView) view.findViewById(R.id.icon);
            container = (LinearLayout) view.findViewById(R.id.container);
            divider = view.findViewById(R.id.divider);
        }
    }

    private void sendRequest(ComponentName name) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent = addIntentExtra(intent);
            intent.setComponent(name);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            mContext.startActivity(intent);
        } catch (IllegalArgumentException e) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent = addIntentExtra(intent);
                mContext.startActivity(Intent.createChooser(intent,
                        mContext.getResources().getString(R.string.email_client)));
            }
            catch (ActivityNotFoundException e1) {
                LogUtil.e(Log.getStackTraceString(e1));
            }
        }
    }

    private Intent addIntentExtra(@NonNull Intent intent) {
        intent.setType("message/rfc822");
        if (mRequest.getStream().length() > 0) {
            File zip = new File(mRequest.getStream());
            Uri uri = FileHelper.getUriFromFile(mContext, mContext.getPackageName(), zip);
            if (uri == null) uri = Uri.fromFile(zip);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{mContext.getResources().getString(R.string.dev_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, mRequest.getSubject());
        intent.putExtra(Intent.EXTRA_TEXT, mRequest.getText());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
    }

}
