package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    private ListView mListView;
    private AsyncTask<Void, Void, Boolean> mGetCredits;
    private int mType;

    private static final String TAG = "candybar.dialog.credits";
    private static final String TYPE = "type";

    public static final int TYPE_ICON_PACK_CONTRIBUTORS = 0;
    public static final int TYPE_DASHBOARD_CONTRIBUTORS = 1;

    private static CreditsFragment newInstance(int type) {
        CreditsFragment fragment = new CreditsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showCreditsDialog(FragmentManager fm, int type) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = CreditsFragment.newInstance(type);
            dialog.show(ft, TAG);
        } catch (IllegalStateException | IllegalArgumentException ignored) {}
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_credits, false);
        builder.typeface("Font-Medium.ttf", "Font-Regular.ttf");
        builder.title(mType == TYPE_ICON_PACK_CONTRIBUTORS ?
                R.string.about_contributors_title : R.string.about_dashboard_contributors);
        builder.positiveText(R.string.close);

        MaterialDialog dialog = builder.build();
        dialog.show();
        mListView = (ListView) dialog.findViewById(R.id.listview);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(TYPE);
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
                        int res = -1;
                        if (mType == TYPE_ICON_PACK_CONTRIBUTORS) {
                            res = R.xml.contributors;
                        } else if (mType == TYPE_DASHBOARD_CONTRIBUTORS) {
                            res = R.xml.dashboard_contributors;
                        }

                        XmlPullParser xpp = getActivity().getResources().getXml(res);

                        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                                if (xpp.getName().equals("contributor")) {
                                    Credit credit = new Credit(
                                            xpp.getAttributeValue(null, "name"),
                                            xpp.getAttributeValue(null, "contribution"),
                                            xpp.getAttributeValue(null, "image"),
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
                    mListView.setAdapter(new CreditsAdapter(getActivity(), credits, mType));
                } else {
                    dismiss();
                }
                mGetCredits = null;
            }
        }.execute();
    }
}
