package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.ChangelogAdapter;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
 *
 * Licensed under the Apache LicenseHelper, Version 2.0 (the "LicenseHelper");
 * you may not use this file except in compliance with the LicenseHelper.
 * You may obtain a copy of the LicenseHelper at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the LicenseHelper is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the LicenseHelper for the specific language governing permissions and
 * limitations under the LicenseHelper.
 */

public class ChangelogFragment extends DialogFragment{

    private static final String TAG = "candybar.dialog.changelog";

    private static ChangelogFragment newInstance() {
        return new ChangelogFragment();
    }

    public static void showChangelog(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment dialog = ChangelogFragment.newInstance();
        dialog.show(ft, TAG);
    }

    private ListView mChangelogList;
    private TextView mChangelogDate;
    private TextView mChangelogVersion;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_changelog, false);
        builder.positiveText(R.string.close);
        MaterialDialog dialog = builder.build();
        dialog.show();

        mChangelogList = (ListView) dialog.findViewById(R.id.changelog_list);
        mChangelogDate = (TextView) dialog.findViewById(R.id.changelog_date);
        mChangelogVersion = (TextView) dialog.findViewById(R.id.changelog_version);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            String version = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0).versionName;
            if (version != null && version.length() > 0) {
                mChangelogVersion.setText(getActivity().getResources().getString(
                        R.string.changelog_version) +" "+ version);
            }
        } catch (Exception ignored) {}

        String date = getActivity().getResources().getString(R.string.changelog_date);
        if (date.length() > 0) mChangelogDate.setText(date);
        else mChangelogDate.setVisibility(View.GONE);

        String[] changelog = getActivity().getResources().getStringArray(R.array.changelog);
        mChangelogList.setAdapter(new ChangelogAdapter(getActivity(), changelog));
    }

}
