package com.dm.material.dashboard.candybar.fragments;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
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
import com.anjlab.android.iab.v3.TransactionDetails;
import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.activities.CandyBarMainActivity;
import com.dm.material.dashboard.candybar.adapters.RequestAdapter;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.fragments.dialog.IntentChooserFragment;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.RequestHelper;
import com.dm.material.dashboard.candybar.helpers.TapIntroHelper;
import com.dm.material.dashboard.candybar.helpers.TypefaceHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.InAppBillingProcessor;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.dm.material.dashboard.candybar.utils.listeners.InAppBillingListener;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.dm.material.dashboard.candybar.helpers.DrawableHelper.getHighQualityIcon;
import static com.dm.material.dashboard.candybar.helpers.ViewHelper.setFastScrollColor;

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

    private MenuItem mMenuItem;
    private RequestAdapter mAdapter;
    private StaggeredGridLayoutManager mManager;
    private AsyncTask mAsyncTask;

    public static List<Integer> sSelectedRequests;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        mRecyclerView = view.findViewById(R.id.request_list);
        mFab =  view.findViewById(R.id.fab);
        mFastScroll = view.findViewById(R.id.fastscroll);
        mProgress = view.findViewById(R.id.progress);

        if (!Preferences.get(getActivity()).isToolbarShadowEnabled()) {
            View shadow = view.findViewById(R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
        resetRecyclerViewPadding(getResources().getConfiguration().orientation);

        mProgress.getIndeterminateDrawable().setColorFilter(
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent),
                PorterDuff.Mode.SRC_IN);

        int color = ColorHelper.getTitleTextColor(ColorHelper
                .getAttributeColor(getActivity(), R.attr.colorAccent));
        mFab.setImageDrawable(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_fab_send, color));
        mFab.setOnClickListener(this);

        if (!Preferences.get(getActivity()).isFabShadowEnabled()) {
            mFab.setCompatElevation(0f);
        }

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mManager = new StaggeredGridLayoutManager(
                getActivity().getResources().getInteger(R.integer.request_column_count),
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mManager);

        setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);

        mAsyncTask = new MissingAppsLoader().execute();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetRecyclerViewPadding(newConfig.orientation);
        if (mAsyncTask != null) return;

        int[] positions = mManager.findFirstVisibleItemPositions(null);

        SparseBooleanArray selectedItems = mAdapter.getSelectedItemsArray();
        ViewHelper.resetSpanCount(mRecyclerView,
                getActivity().getResources().getInteger(R.integer.request_column_count));

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
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_select_all) {
            mMenuItem = item;
            if (mAdapter == null) return false;
            if (mAdapter.selectAll()) {
                item.setIcon(R.drawable.ic_toolbar_select_all_selected);
                return true;
            }

            item.setIcon(R.drawable.ic_toolbar_select_all);
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

                boolean requestLimit = getResources().getBoolean(
                        R.bool.enable_icon_request_limit);
                boolean iconRequest = getResources().getBoolean(
                        R.bool.enable_icon_request);
                boolean premiumRequest =getResources().getBoolean(
                        R.bool.enable_premium_request);

                if (Preferences.get(getActivity()).isPremiumRequest()) {
                    int count = Preferences.get(getActivity()).getPremiumRequestCount();
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
                    int used = Preferences.get(getActivity()).getRegularRequestUsed();
                    if (selected > (limit - used)) {
                        RequestHelper.showIconRequestLimitDialog(getActivity());
                        return;
                    }
                }

                mAsyncTask = new RequestLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                Toast.makeText(getActivity(), R.string.request_not_selected,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void resetRecyclerViewPadding(int orientation) {
        if (mRecyclerView == null) return;

        int padding = 0;
        boolean tabletMode = getResources().getBoolean(R.bool.android_helpers_tablet_mode);
        if (tabletMode || orientation == Configuration.ORIENTATION_LANDSCAPE) {
            padding = getActivity().getResources().getDimensionPixelSize(R.dimen.content_padding);

            if (CandyBarApplication.getConfiguration().getRequestStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT) {
                padding = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            }
        }

        int size = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_size);
        int marginGlobal = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_margin_global);

        mRecyclerView.setPadding(padding, padding, 0, size + (marginGlobal * 2));
    }

    public void prepareRequest() {
        if (mAsyncTask != null) return;

        mAsyncTask = new RequestLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void refreshIconRequest() {
        if (mAdapter == null) {
            RequestFragment.sSelectedRequests = null;
            return;
        }

        if (RequestFragment.sSelectedRequests == null)
            mAdapter.notifyItemChanged(0);

        for (Integer integer : RequestFragment.sSelectedRequests) {
            mAdapter.setRequested(integer, true);
        }

        mAdapter.notifyDataSetChanged();
        RequestFragment.sSelectedRequests = null;
    }

    private class MissingAppsLoader extends AsyncTask<Void, Void, Boolean> {

        private List<Request> requests;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (CandyBarMainActivity.sMissedApps == null) {
                mProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    if (CandyBarMainActivity.sMissedApps == null) {
                        CandyBarMainActivity.sMissedApps = RequestHelper.getMissingApps(getActivity());
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
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            mProgress.setVisibility(View.GONE);
            if (aBoolean) {
                setHasOptionsMenu(true);
                mAdapter = new RequestAdapter(getActivity(),
                        requests, mManager.getSpanCount());
                mRecyclerView.setAdapter(mAdapter);

                AnimationHelper.show(mFab)
                        .interpolator(new LinearOutSlowInInterpolator())
                        .start();

                TapIntroHelper.showRequestIntro(getActivity(), mRecyclerView);
            } else {
                mRecyclerView.setAdapter(null);
                Toast.makeText(getActivity(), R.string.request_appfilter_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class RequestLoader extends AsyncTask<Void, Void, Boolean> {

        private MaterialDialog dialog;
        private boolean noEmailClientError = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
            builder.typeface(
                    TypefaceHelper.getMedium(getActivity()),
                    TypefaceHelper.getRegular(getActivity()));
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
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                            getResources().getString(R.string.dev_email),
                            null));
                    List<ResolveInfo> resolveInfos = getActivity().getPackageManager()
                            .queryIntentActivities(intent, 0);
                    if (resolveInfos.size() == 0) {
                        noEmailClientError = true;
                        return false;
                    }

                    if (Preferences.get(getActivity()).isPremiumRequest()) {
                        TransactionDetails details = InAppBillingProcessor.get(getActivity())
                                .getProcessor().getPurchaseTransactionDetails(
                                Preferences.get(getActivity()).getPremiumRequestProductId());
                        if (details == null) return false;

                        CandyBarApplication.sRequestProperty = new Request.Property(null,
                                details.purchaseInfo.purchaseData.orderId,
                                details.purchaseInfo.purchaseData.productId);
                    }

                    RequestFragment.sSelectedRequests = mAdapter.getSelectedItems();
                    List<Request> requests = mAdapter.getSelectedApps();
                    File appFilter = RequestHelper.buildXml(getActivity(), requests, RequestHelper.XmlType.APPFILTER);
                    File appMap = RequestHelper.buildXml(getActivity(), requests, RequestHelper.XmlType.APPMAP);
                    File themeResources = RequestHelper.buildXml(getActivity(), requests, RequestHelper.XmlType.THEME_RESOURCES);

                    File directory = getActivity().getCacheDir();
                    List<String> files = new ArrayList<>();

                    for (Request request : requests) {
                        Drawable drawable = getHighQualityIcon(getActivity(), request.getPackageName());
                        String icon = IconsHelper.saveIcon(files, directory, drawable, request.getName());
                        if (icon != null) files.add(icon);
                    }

                    if (appFilter != null) {
                        files.add(appFilter.toString());
                    }

                    if (appMap != null) {
                        files.add(appMap.toString());
                    }

                    if (themeResources != null) {
                        files.add(themeResources.toString());
                    }

                    CandyBarApplication.sZipPath = FileHelper.createZip(files, new File(directory.toString(),
                            RequestHelper.getGeneratedZipName(RequestHelper.ZIP)));
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
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            dialog.dismiss();
            if (aBoolean) {
                IntentChooserFragment.showIntentChooserDialog(getActivity().getSupportFragmentManager(),
                        IntentChooserFragment.ICON_REQUEST);

                mAdapter.resetSelectedItems();
                if (mMenuItem != null) mMenuItem.setIcon(R.drawable.ic_toolbar_select_all);
            } else {
                if (noEmailClientError) {
                    Toast.makeText(getActivity(), R.string.no_email_app,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.request_build_failed,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
