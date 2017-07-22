package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.IntentAdapter;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.fragments.RequestFragment;
import com.dm.material.dashboard.candybar.helpers.RequestHelper;
import com.dm.material.dashboard.candybar.helpers.TypefaceHelper;
import com.dm.material.dashboard.candybar.items.IntentChooser;
import com.dm.material.dashboard.candybar.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

public class IntentChooserFragment extends DialogFragment {

    private ListView mIntentList;
    private TextView mNoApp;

    private int mType;
    private IntentAdapter mAdapter;
    private AsyncTask<Void, Void, Boolean> mLoadIntentChooser;

    public static final int ICON_REQUEST = 0;
    public static final int REBUILD_ICON_REQUEST = 1;
    public static final String TAG = "candybar.dialog.intent.chooser";
    private static final String TYPE = "type";

    private static IntentChooserFragment newInstance(int type) {
        IntentChooserFragment fragment = new IntentChooserFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showIntentChooserDialog(@NonNull FragmentManager fm, int type) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = IntentChooserFragment.newInstance(type);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {}
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(TYPE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_intent_chooser, false);
        builder.typeface(
                TypefaceHelper.getMedium(getActivity()),
                TypefaceHelper.getRegular(getActivity()));
        builder.positiveText(android.R.string.cancel);

        MaterialDialog dialog = builder.build();
        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(view -> {
            if (mAdapter == null || mAdapter.isAsyncTaskRunning()) return;

            File file = new File(getActivity().getCacheDir(), RequestHelper.ZIP);
            if (file.exists()){
                if (file.delete()) {
                    LogUtil.e("Intent chooser cancel: icon_request.zip deleted");
                }
            }

            File file1 = new File(getActivity().getCacheDir(), RequestHelper.REBUILD_ZIP);
            if (file1.exists()) {
                if (file1.delete()) {
                    if (file1.delete()) {
                        LogUtil.e("Intent chooser cancel: rebuild_icon_request.zip deleted");
                    }
                }
            }

            RequestFragment.sSelectedRequests = null;
            CandyBarApplication.sRequestProperty = null;
            dialog.dismiss();
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        setCancelable(false);

        mIntentList = (ListView) dialog.findViewById(R.id.intent_list);
        mNoApp = (TextView) dialog.findViewById(R.id.intent_noapp);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadIntentChooser();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mLoadIntentChooser != null) mLoadIntentChooser.cancel(true);
        super.onDismiss(dialog);
    }

    private void loadIntentChooser() {
        mLoadIntentChooser = new AsyncTask<Void, Void, Boolean>() {

            List<IntentChooser> apps;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                apps = new ArrayList<>();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while(!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                                getActivity().getResources().getString(R.string.dev_email),
                                null));
                        List<ResolveInfo> resolveInfos = getActivity().getPackageManager()
                                .queryIntentActivities(intent, 0);
                        try {
                            Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(
                                    getActivity().getPackageManager()));
                        } catch (Exception ignored){}

                        for (ResolveInfo resolveInfo : resolveInfos) {
                            switch (resolveInfo.activityInfo.packageName) {
                                case "com.google.android.gm":
                                    apps.add(new IntentChooser(resolveInfo, IntentChooser.TYPE_RECOMMENDED));
                                    break;
                                case "com.google.android.apps.inbox":
                                    try {
                                        ComponentName componentName = new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                                                "com.google.android.apps.bigtop.activities.MainActivity");
                                        Intent inbox = new Intent(Intent.ACTION_SEND);
                                        inbox.setComponent(componentName);

                                        List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(
                                                inbox, PackageManager.MATCH_DEFAULT_ONLY);
                                        if (list.size() > 0) {
                                            apps.add(new IntentChooser(resolveInfo, IntentChooser.TYPE_SUPPORTED));
                                            break;
                                        }
                                    } catch (ActivityNotFoundException e) {
                                        LogUtil.e(Log.getStackTraceString(e));
                                    }

                                    apps.add(new IntentChooser(resolveInfo, IntentChooser.TYPE_NOT_SUPPORTED));
                                    break;
                                case "com.android.fallback":
                                case "com.paypal.android.p2pmobile":
                                case "com.lonelycatgames.Xplore":
                                    break;
                                default:
                                    apps.add(new IntentChooser(resolveInfo, IntentChooser.TYPE_SUPPORTED));
                                    break;
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        LogUtil.e(Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean && apps != null) {
                    mAdapter = new IntentAdapter(getActivity(), apps, mType);
                    mIntentList.setAdapter(mAdapter);

                    if (apps.size() == 0) {
                        mNoApp.setVisibility(View.VISIBLE);
                        setCancelable(true);
                    }
                } else {
                    dismiss();
                    Toast.makeText(getActivity(), R.string.intent_email_failed,
                            Toast.LENGTH_LONG).show();
                }
                mLoadIntentChooser = null;
            }
        }.execute();
    }
}
