package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.adapters.RequestAdapter;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DeviceHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.FileHelper;
import com.dm.material.dashboard.candybar.helpers.RequestHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.Animator;
import com.dm.material.dashboard.candybar.utils.LogUtil;
import com.dm.material.dashboard.candybar.utils.listeners.InAppBillingListener;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

public class RequestFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private RecyclerFastScroller mFastScroll;
    private ProgressBar mProgress;

    private RequestAdapter mAdapter;
    private StaggeredGridLayoutManager mManager;
    private AsyncTask<Void, Request, Boolean> mGetMissingApps;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.request_list);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
        resetRecyclerViewPadding(getActivity().getResources().getConfiguration().orientation);

        mProgress.getIndeterminateDrawable().setColorFilter(
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent),
                PorterDuff.Mode.SRC_IN);

        int color = ColorHelper.getTitleTextColor(ColorHelper
                .getAttributeColor(getActivity(), R.attr.colorAccent));
        mFab.setImageDrawable(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_fab_send, color));
        mFab.setOnClickListener(this);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.setHasFixedSize(false);

        mManager = new StaggeredGridLayoutManager(
                getActivity().getResources().getInteger(R.integer.request_column_count),
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mManager);

        ViewHelper.setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);

        getMissingApps();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetRecyclerViewPadding(newConfig.orientation);
        if (mGetMissingApps != null) return;

        int[] positions = mManager.findFirstVisibleItemPositions(null);

        SparseBooleanArray selectedItems = mAdapter.getSelectedItemsArray();
        ViewHelper.resetSpanCount(mRecyclerView, R.integer.request_column_count);

        mAdapter = new RequestAdapter(getActivity(),
                CandyBarMainActivity.sMissedApps,
                mManager.getSpanCount());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setSelectedItemsArray(selectedItems);

        if (positions.length > 0)
            mRecyclerView.scrollToPosition(positions[0]);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_request, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        if (mGetMissingApps != null) mGetMissingApps.cancel(true);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_select_all) {
            if (mAdapter == null) return false;
            mAdapter.selectAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fab) {
            if (mAdapter == null) return;

            int selected = mAdapter.getSelectedItemsSize();
            if (selected > 0) {
                if (mAdapter.isContainsRequested()) {
                    RequestHelper.showAlreadyRequestedDialog(getActivity());
                    return;
                }

                boolean requestLimit = getActivity().getResources().getBoolean(
                        R.bool.enable_icon_request_limit);
                boolean iconRequest = getActivity().getResources().getBoolean(
                        R.bool.enable_icon_request);
                boolean premiumRequest = getActivity().getResources().getBoolean(
                        R.bool.enable_premium_request);

                if (Preferences.getPreferences(getActivity()).isPremiumRequest()) {
                    int count = Preferences.getPreferences(getActivity()).getPremiumRequestCount();
                    if (selected > count) {
                        RequestHelper.showPremiumRequestLimitDialog(getActivity(), selected);
                        return;
                    }

                    if (!RequestHelper.isReadyToSendPremiumRequest(getActivity())) return;

                    try {
                        InAppBillingListener listener = (InAppBillingListener) getActivity();
                        listener.onInAppBillingRequest();
                    } catch (Exception ignored) {}
                    return;
                }

                if (!iconRequest && premiumRequest) {
                    RequestHelper.showPremiumRequestRequired(getActivity());
                    return;
                }

                if (requestLimit) {
                    int limit = getActivity().getResources().getInteger(R.integer.icon_request_limit);
                    int used = Preferences.getPreferences(getActivity()).getRegularRequestUsed();
                    if (selected > (limit - used)) {
                        RequestHelper.showIconRequestLimitDialog(getActivity());
                        return;
                    }

                    Preferences.getPreferences(getActivity()).setRegularRequestUsed((used + selected));
                }

                sendRequest(null);
            } else {
                Toast.makeText(getActivity(), R.string.request_not_selected,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void resetRecyclerViewPadding(int orientation) {
        if (mRecyclerView == null) return;

        int padding = 0;
        boolean tabletMode = getActivity().getResources().getBoolean(R.bool.tablet_mode);
        if (tabletMode || orientation == Configuration.ORIENTATION_LANDSCAPE) {
            padding = getActivity().getResources().getDimensionPixelSize(R.dimen.content_padding);
        }

        int size = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_size);
        int marginGlobal = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_margin_global);

        mRecyclerView.setPadding(padding, padding, 0, size + (marginGlobal * 2));
    }

    public void onInAppBillingSent(BillingProcessor billingProcessor) {
        sendRequest(billingProcessor);
    }

    public void premiumRequestBought() {
        if (mAdapter == null || !Preferences.getPreferences(getActivity()).isPremiumRequestEnabled()) return;

        mAdapter.notifyItemChanged(0);
    }

    private void getMissingApps() {
        mGetMissingApps = new AsyncTask<Void, Request, Boolean>() {

            List<Request> requests;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (CandyBarMainActivity.sMissedApps == null) {
                            CandyBarMainActivity.sMissedApps = RequestHelper.loadMissingApps(getActivity());
                        }

                        requests = CandyBarMainActivity.sMissedApps;
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
                mProgress.setVisibility(View.GONE);
                if (aBoolean) {
                    setHasOptionsMenu(true);
                    mAdapter = new RequestAdapter(getActivity(),
                            requests, mManager.getSpanCount());
                    mRecyclerView.setAdapter(mAdapter);

                    Animator.showFab(mFab);
                } else {
                    mRecyclerView.setAdapter(null);
                    Toast.makeText(getActivity(), getActivity().getResources().getString(
                            R.string.request_appfilter_failed), Toast.LENGTH_LONG).show();
                }
                mGetMissingApps = null;
            }

        }.execute();
    }

    private void sendRequest(BillingProcessor billingProcessor) {
        new AsyncTask<Void, Void, Boolean>() {

            MaterialDialog dialog;
            StringBuilder sb;
            String zipFile;
            String productId = "";
            String orderId = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sb = new StringBuilder();

                MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
                builder.content(R.string.request_building);
                builder.cancelable(false);
                builder.canceledOnTouchOutside(false);
                builder.progress(true, 0);
                builder.progressIndeterminateStyle(true);
                dialog = builder.build();
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        Database database = new Database(getActivity());
                        File directory = getActivity().getCacheDir();
                        sb.append(DeviceHelper.getDeviceInfo(getActivity()));

                        if (Preferences.getPreferences(getActivity()).isPremiumRequest()) {
                            if (billingProcessor == null) return false;
                            TransactionDetails details = billingProcessor.getPurchaseTransactionDetails(
                                    Preferences.getPreferences(getActivity()).getPremiumRequestProductId());
                            if (details != null) {
                                orderId = details.purchaseInfo.purchaseData.orderId;
                                productId = details.purchaseInfo.purchaseData.productId;
                                sb.append("Order Id : ").append(orderId)
                                        .append("\nProduct Id : ").append(productId)
                                        .append("\n");
                            }
                        }

                        List<Integer> selectedItems = mAdapter.getSelectedItems();
                        List<String> files = new ArrayList<>();
                        File appFilter = new File(directory.toString() + "/" + "appfilter.xml");

                        Writer out = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(appFilter), "UTF8"));

                        for (int i = 0; i < selectedItems.size(); i++) {
                            Request item = mAdapter.getRequest(selectedItems.get(i));
                            database.addRequest(item.getName(), item.getActivity(), null);
                            mAdapter.setRequested(selectedItems.get(i), true);

                            String string = RequestHelper.writeRequest(item);
                            sb.append(string);

                            String string1 = RequestHelper.writeAppFilter(item);
                            out.append(string1);

                            Drawable drawable = DrawableHelper.getHighQualityIcon(
                                    getActivity(), item.getPackageName());

                            String icon = FileHelper.saveIcon(files, directory, drawable, item.getName());
                            if (icon != null) files.add(icon);

                            if (Preferences.getPreferences(getActivity()).isPremiumRequest()) {
                                database.addPremiumRequest(
                                        null,
                                        orderId,
                                        productId,
                                        item.getName(),
                                        item.getActivity(),
                                        null);
                            }
                        }

                        out.flush();
                        out.close();
                        files.add(appFilter.toString());

                        zipFile = FileHelper.createZip(files, directory.toString() + "/" + "icon_request.zip");
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
                dialog.dismiss();
                if (aBoolean) {
                    String subject = Preferences.getPreferences(getActivity()).isPremiumRequest() ?
                            "Premium Icon Request " : "Icon Request ";
                    subject = subject + getActivity().getResources().getString(R.string.app_name);

                    Request request = new Request(subject, sb.toString(),
                            zipFile, mAdapter.getSelectedItemsSize());
                    try {
                        RequestListener listener = (RequestListener) getActivity();
                        listener.onRequestBuilt(request);
                    } catch (Exception ignored) {}
                    mAdapter.resetSelectedItems();
                } else {
                    Toast.makeText(getActivity(), R.string.request_build_failed,
                            Toast.LENGTH_LONG).show();
                }
                dialog = null;
                sb.setLength(0);
                sb.trimToSize();
            }

        }.execute();
    }
}
