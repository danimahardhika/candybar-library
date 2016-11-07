package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.utils.Tag;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
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

public class LicensesFragment extends DialogFragment {

    private static final String TAG = "candybar.dialog.licenses";

    private static LicensesFragment newInstance() {
        return new LicensesFragment();
    }

    public static void showLicensesDialog(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment dialog = LicensesFragment.newInstance();
        dialog.show(ft, TAG);
    }

    private WebView mWebView;

    private AsyncTask<Void, Void, Boolean> mLoadLicenses;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_licenses, false);
        builder.title(R.string.open_source_licenses);
        MaterialDialog dialog = builder.build();
        dialog.show();

        mWebView = (WebView) dialog.findViewById(R.id.webview);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadLicenses();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mLoadLicenses != null) mLoadLicenses.cancel(true);
    }

    private void loadLicenses() {
        mLoadLicenses = new AsyncTask<Void, Void, Boolean>() {

            StringBuilder sb;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sb = new StringBuilder();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        InputStream rawResource = getActivity().getResources()
                                .openRawResource(R.raw.licenses);
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(rawResource));

                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            sb.append(line);
                            sb.append("\n");
                        }
                        bufferedReader.close();
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
                if (aBoolean) {
                    mWebView.setVisibility(View.VISIBLE);
                    mWebView.loadDataWithBaseURL(null,
                            sb.toString(), "text/html", "utf-8", null);
                }
                mLoadLicenses = null;
            }

        }.execute();
    }

}


