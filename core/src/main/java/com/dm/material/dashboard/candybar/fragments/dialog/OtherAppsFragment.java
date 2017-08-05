package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.OtherAppsAdapter;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.helpers.TypefaceHelper;

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

public class OtherAppsFragment extends DialogFragment {

    private ListView mListView;

    private static final String TAG = "candybar.dialog.otherapps";

    private static OtherAppsFragment newInstance() {
        return new OtherAppsFragment();
    }

    public static void showOtherAppsDialog(@NonNull FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = OtherAppsFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalStateException | IllegalArgumentException ignored) {}
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_other_apps, false);
        builder.typeface(
                TypefaceHelper.getMedium(getActivity()),
                TypefaceHelper.getRegular(getActivity()));
        builder.title(R.string.home_more_apps_header);
        builder.positiveText(R.string.close);

        MaterialDialog dialog = builder.build();
        dialog.show();

        mListView = (ListView) dialog.findViewById(R.id.listview);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<CandyBarApplication.OtherApp> otherApps = CandyBarApplication.getConfiguration().getOtherApps();
        if (otherApps != null) {
            mListView.setAdapter(new OtherAppsAdapter(getActivity(), otherApps));
            return;
        }

        dismiss();
    }
}
