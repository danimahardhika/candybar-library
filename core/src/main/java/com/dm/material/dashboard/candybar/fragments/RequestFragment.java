package com.dm.material.dashboard.candybar.fragments;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.dm.material.dashboard.candybar.utils.Tag;
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

    private RecyclerView mRequestList;
    private FloatingActionButton mFab;
    private RecyclerFastScroller mFastScroll;
    private ProgressBar mProgress;
    private AppCompatButton mBuy;
    private TextView mDesc;
    private TextView mCount;

    private RequestAdapter mAdapter;
    private AsyncTask<Void, Request, Boolean> mGetMissingApps;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        mRequestList = (RecyclerView) view.findViewById(R.id.request_list);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mBuy = (AppCompatButton) view.findViewById(R.id.premium_request_buy);
        mDesc = (TextView) view.findViewById(R.id.premium_request_desc);
        mCount = (TextView) view.findViewById(R.id.premium_request_count);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
        ViewCompat.setNestedScrollingEnabled(mRequestList, false);
        resetNavigationBarMargin();

        mProgress.getIndeterminateDrawable().setColorFilter(
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent),
                PorterDuff.Mode.SRC_IN);

        int color = ColorHelper.getTitleTextColor(ColorHelper
                .getAttributeColor(getActivity(), R.attr.colorAccent));
        mFab.setImageDrawable(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_fab_send, color));
        mFab.setOnClickListener(this);

        mRequestList.setItemAnimator(new DefaultItemAnimator());
        mRequestList.getItemAnimator().setChangeDuration(0);
        mRequestList.setHasFixedSize(false);
        mRequestList.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.request_column_count)));
        mFastScroll.attachRecyclerView(mRequestList);

        initPremiumRequest();
        getMissingApps();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetNavigationBarMargin();
        ViewHelper.resetSpanCount(getActivity(), mRequestList, R.integer.request_column_count);
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
                        listener.OnInAppBillingRequest();
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

                    Preferences.getPreferences(getActivity()).setRegularRequestUsed(selected);
                }

                sendRequest(null);
            } else {
                Toast.makeText(getActivity(), R.string.request_not_selected,
                        Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.premium_request_buy) {
            RequestListener listener = (RequestListener) getActivity();
            listener.OnBuyPremiumRequest();
        }
    }

    private void initPremiumRequest() {
        boolean premiumRequest = Preferences.getPreferences(getActivity()).isPremiumRequestEnabled();
        if (premiumRequest) {
            LinearLayout premiumRequestBar = (LinearLayout) getActivity().findViewById(R.id.premium_request_bar);
            premiumRequestBar.setVisibility(View.VISIBLE);

            int accent = ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent);
            mBuy.setTextColor(ColorHelper.getTitleTextColor(accent));
            mBuy.setOnClickListener(this);

            int toolbarIcon = ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon);
            mDesc.setTextColor(ColorHelper.setColorAlpha(toolbarIcon, 0.6f));

            initPremiumRequestCount();
        }
    }

    private void initPremiumRequestCount() {
        if (Preferences.getPreferences(getActivity()).isPremiumRequest()) {
            String countText = getActivity().getResources().getString(R.string.premium_request_count)
                    +" "+ Preferences.getPreferences(getActivity()).getPremiumRequestCount();
            mCount.setText(countText);
            mCount.setVisibility(View.VISIBLE);
            mBuy.setVisibility(View.GONE);
            return;
        }

        mCount.setVisibility(View.GONE);
        mBuy.setVisibility(View.VISIBLE);
    }

    public void OnInAppBillingSent(BillingProcessor billingProcessor) {
        sendRequest(billingProcessor);
    }

    public void premiumRequestBought() {
        initPremiumRequestCount();
    }

    private void resetNavigationBarMargin() {
        int padding = getActivity().getResources().getDimensionPixelSize(R.dimen.content_padding);
        int size = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_size);
        int margin = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_margin);
        int marginGlobal = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_margin_global);
        int navBar = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (getActivity().getResources().getBoolean(R.bool.use_translucent_navigation_bar)) {
                navBar = ViewHelper.getNavigationBarHeight(getActivity());
            }
        }

        boolean tabletMode = getActivity().getResources().getBoolean(R.bool.tablet_mode);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mFab.getLayoutParams();

        if (tabletMode || getActivity().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {
            mRequestList.setPadding(padding, padding, padding, (padding + size + marginGlobal + navBar));
            params.setMargins(0, 0, margin, (margin + navBar));
        } else {
            mRequestList.setPadding(padding, padding, padding, (padding + size + marginGlobal));
            params.setMargins(0, 0, margin, margin);
        }

        mFab.setLayoutParams(params);
    }

    private void getMissingApps() {
        mGetMissingApps = new AsyncTask<Void, Request, Boolean>() {

            List<Request> requests;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (CandyBarMainActivity.sMissingApps == null)
                    mProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (CandyBarMainActivity.sMissingApps == null) {
                            CandyBarMainActivity.sMissingApps = RequestHelper.loadMissingApps(getActivity());
                        }

                        requests = CandyBarMainActivity.sMissingApps;
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
                mProgress.setVisibility(View.GONE);
                if (aBoolean) {
                    setHasOptionsMenu(true);
                    mAdapter = new RequestAdapter(getActivity(), requests);
                    mRequestList.setAdapter(mAdapter);
                    Animator.showFab(mFab);
                } else {
                    mRequestList.setAdapter(null);
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

                            Bitmap bitmap = DrawableHelper.getHighQualityIcon(
                                    getActivity(), item.getPackageName());

                            String icon = FileHelper.saveIcon(directory, bitmap, item.getName());
                            if (icon != null) files.add(icon);

                            if (Preferences.getPreferences(getActivity()).isPremiumRequest()) {
                                database.addPremiumRequest(orderId, productId, item.getName(), item.getActivity());
                            }
                        }

                        out.flush();
                        out.close();
                        files.add(appFilter.toString());

                        zipFile = directory.toString() + "/" + "icon_request.zip";
                        FileHelper.createZip(files, zipFile);
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
                dialog.dismiss();
                if (aBoolean) {
                    String subject = Preferences.getPreferences(getActivity()).isPremiumRequest() ?
                            "Premium Icon Request " : "Icon Request ";
                    subject = subject + getActivity().getResources().getString(R.string.app_name);

                    Request request = new Request(subject, sb.toString(),
                            zipFile, mAdapter.getSelectedItemsSize());
                    try {
                        RequestListener listener = (RequestListener) getActivity();
                        listener.OnRequestBuilt(request);
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
