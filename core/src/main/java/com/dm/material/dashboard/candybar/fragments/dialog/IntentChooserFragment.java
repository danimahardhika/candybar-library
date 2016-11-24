package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.IntentAdapter;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.utils.Tag;

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

    private static final String SUBJECT = "subject";
    private static final String TEXT = "text";
    private static final String STREAM = "stream";

    public static final String TAG = "candybar.dialog.intent.choose";

    private static IntentChooserFragment newInstance(Request request) {
        IntentChooserFragment fragment = new IntentChooserFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SUBJECT, request.getSubject());
        bundle.putString(TEXT, request.getText());
        bundle.putString(STREAM, request.getStream());
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showIntentChooserDialog(@NonNull FragmentManager fm, @NonNull Request request) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment dialog = IntentChooserFragment.newInstance(request);
        dialog.show(ft, TAG);
    }

    private ListView mIntentList;
    private TextView mNoApp;

    private Request mRequest;
    private AsyncTask<Void, Void, Boolean> mLoadIntentChooser;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_intent_chooser, false);
        MaterialDialog dialog = builder.build();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        setCancelable(false);

        mIntentList = (ListView) dialog.findViewById(R.id.intent_list);
        mNoApp = (TextView) dialog.findViewById(R.id.intent_noapp);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String subject = getArguments().getString(SUBJECT);
        String text = getArguments().getString(TEXT);
        String stream = getArguments().getString(STREAM);
        mRequest = new Request(subject, text, stream);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            String subject = savedInstanceState.getString(SUBJECT);
            String text = savedInstanceState.getString(TEXT);
            String stream = savedInstanceState.getString(STREAM);
            mRequest = new Request(subject, text, stream);
        }

        if (mRequest != null) loadIntentChooser();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mLoadIntentChooser != null) mLoadIntentChooser.cancel(true);
        super.onDismiss(dialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SUBJECT, mRequest.getSubject());
        outState.putString(TEXT, mRequest.getText());
        outState.putString(STREAM, mRequest.getStream());
        super.onSaveInstanceState(outState);
    }

    private void loadIntentChooser() {
        mLoadIntentChooser = new AsyncTask<Void, Void, Boolean>() {

            Intent intent;
            List<ResolveInfo> apps;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                        getActivity().getResources().getString(R.string.dev_email),
                        null));
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while(!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        apps = getActivity().getPackageManager()
                                .queryIntentActivities(intent, 0);
                        try {
                            Collections.sort(apps, new ResolveInfo.DisplayNameComparator(
                                    getActivity().getPackageManager()));
                        } catch (Exception ignored){}
                        return true;
                    } catch (Exception e) {
                        Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
                        return false;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean && apps != null) {
                    IntentAdapter adapter = new IntentAdapter(getActivity(),
                            apps, mRequest);
                    mIntentList.setAdapter(adapter);

                    if (adapter.getCount() == 0) {
                        mNoApp.setVisibility(View.VISIBLE);
                        setCancelable(true);
                    }
                } else {
                    dismiss();
                    Toast.makeText(getActivity(), R.string.failed_email_app,
                            Toast.LENGTH_LONG).show();
                }
                mLoadIntentChooser = null;
            }
        }.execute();
    }

}
