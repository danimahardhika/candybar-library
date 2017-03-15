package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.CreditsAdapter;
import com.dm.material.dashboard.candybar.items.Credit;
import com.dm.material.dashboard.candybar.utils.LogUtil;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
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

public class CreditsFragment extends DialogFragment {

    private static final String TAG = "candybar.dialog.credits";

    private static CreditsFragment newInstance() {
        return new CreditsFragment();
    }

    public static void showCreditsDialog(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = CreditsFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalStateException | IllegalArgumentException ignored) {}
    }

    private ListView mListView;

    private AsyncTask<Void, Void, Boolean> mGetCredits;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_credits, false);
        builder.title(R.string.about_contributors_title);
        builder.positiveText(R.string.close);

        MaterialDialog dialog = builder.build();
        dialog.show();
        mListView = (ListView) dialog.findViewById(R.id.listview);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getData();
    }

    @Override
    public void onDestroyView() {
        if (mGetCredits != null) mGetCredits.cancel(true);
        super.onDestroyView();
    }

    private void getData() {
        mGetCredits = new AsyncTask<Void, Void, Boolean>() {

            List<Credit> credits;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                credits = new ArrayList<>();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        XmlPullParser xpp = getActivity().getResources().getXml(R.xml.contributors);

                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("contributor")) {
                                    Credit credit = new Credit(
                                            xpp.getAttributeValue(null, "name"),
                                            xpp.getAttributeValue(null, "contribution"),
                                            xpp.getAttributeValue(null, "link"));
                                    credits.add(credit);
                                }
                            }
                            xpp.next();
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
                if (aBoolean) {
                    mListView.setAdapter(new CreditsAdapter(getActivity(), credits));
                } else {
                    dismiss();
                }
                mGetCredits = null;
            }
        }.execute();
    }

}
