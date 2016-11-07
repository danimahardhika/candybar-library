package com.dm.material.dashboard.candybar.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.RequestAdapter;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.fragments.dialog.IntentChooserFragment;
import com.dm.material.dashboard.candybar.helpers.AppFilterHelper;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.FileHelper;
import com.dm.material.dashboard.candybar.helpers.PermissionHelper;
import com.dm.material.dashboard.candybar.helpers.RequestHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.preferences.Preferences;
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
import java.util.Collections;
import java.util.List;

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

public class RequestFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mRequestList;
    private RecyclerFastScroller mFastScroll;
    private ProgressBar mProgress;
    private FloatingActionButton mFab;

    private RequestAdapter mAdapter;
    private Database mDatabase;
    private File mDirectory;
    private List<String> mFiles;
    private AsyncTask<Void, Void, Boolean> mGetMissingApps;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        mRequestList = (RecyclerView) view.findViewById(R.id.request_list);
        mFastScroll = (RecyclerFastScroller) view.findViewById(R.id.fastscroll);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
        ViewCompat.setNestedScrollingEnabled(mRequestList, false);
        resetNavigationBarMargin();

        mFiles = new ArrayList<>();
        mDatabase = new Database(getActivity());

        mProgress.getIndeterminateDrawable().setColorFilter(
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent),
                PorterDuff.Mode.SRC_IN);
        mFab.setOnClickListener(this);
        int color = ColorHelper.getTitleTextColor(getActivity(),
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent));
        mFab.setImageDrawable(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_fab_send, color));

        mRequestList.setItemAnimator(new DefaultItemAnimator());
        mRequestList.getItemAnimator().setChangeDuration(0);
        mRequestList.setHasFixedSize(false);
        mRequestList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFastScroll.attachRecyclerView(mRequestList);

        mDirectory = FileHelper.getCacheDirectory(getActivity());
        getMissingApps();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetNavigationBarMargin();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_request, menu);
        MenuItem rebuild = menu.findItem(R.id.menu_rebuild_premium);
        boolean premium = getActivity().getResources().getBoolean(R.bool.enable_premium_request);
        rebuild.setVisible(premium);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGetMissingApps != null) mGetMissingApps.cancel(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_rebuild_premium) {
            rebuildPremiumRequest();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fab) {
            int selected = mAdapter.getSelectedItemsSize();
            if (selected > 0) {
                if (mAdapter.isContainsRequested()) {
                    RequestHelper.showAlreadyRequestedDialog(getActivity());
                    return;
                } else {
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

                    boolean requestLimit = getActivity().getResources().getBoolean(
                            R.bool.enable_icon_request_limit);
                    if (requestLimit) {
                        int limit = getActivity().getResources().getInteger(R.integer.icon_request_limit);
                        int used = Preferences.getPreferences(getActivity()).getRegularRequestUsed();
                        if (selected > limit || selected > (limit - used)) {
                            RequestHelper.showIconRequestLimitDialog(getActivity());
                            return;
                        }

                        Preferences.getPreferences(getActivity()).setRegularRequestUsed(selected);
                    }
                }

                if (PermissionHelper.isPermissionStorageGranted(getActivity())) {
                    sendRequest(null);
                    return;
                }

                PermissionHelper.showRequestPermissionStorageDenied(getActivity());
            } else {
                Toast.makeText(getActivity(), R.string.request_not_selected,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void OnInAppBillingSent(BillingProcessor billingProcessor) {
        sendRequest(billingProcessor);
    }

    public void refreshAdapter() {
        if (mAdapter != null)
            mAdapter.resetAdapter();
    }

    private void resetNavigationBarMargin() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int padding = getActivity().getResources().getDimensionPixelSize(R.dimen.content_padding);
            int size = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_size);
            int margin = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_margin);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            if (getActivity().getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT) {
                int navbar = ViewHelper.getNavigationBarHeight(getActivity());
                mRequestList.setPadding(padding, padding, padding, (padding + navbar + size + margin));
                params.setMargins(0, 0, margin, (margin + navbar));
            } else {
                mRequestList.setPadding(padding, padding, padding, (padding + size + margin));
                params.setMargins(0, 0, margin, margin);
            }
            params.gravity = GravityCompat.END | Gravity.BOTTOM;
            mFab.setLayoutParams(params);
        }
    }

    private void getMissingApps() {
        mGetMissingApps = new AsyncTask<Void, Void, Boolean>() {

            List<Request> requests;
            List<ResolveInfo> apps;
            Intent intent;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgress.setVisibility(View.VISIBLE);
                requests = new ArrayList<>();
                apps = new ArrayList<>();
                intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);

                        StringBuilder activities = AppFilterHelper.loadAppFilter(getActivity());

                        apps = getActivity().getPackageManager().queryIntentActivities(
                                intent, PackageManager.GET_RESOLVED_FILTER);
                        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(
                                getActivity().getPackageManager()));

                        for (ResolveInfo app : apps) {
                            String name = app.activityInfo.loadLabel(getActivity()
                                    .getPackageManager()).toString();
                            String activity = app.activityInfo.packageName +"/"+ app.activityInfo.name;
                            if (!activities.toString().contains(activity)) {
                                Drawable drawable = DrawableHelper.getAppIcon(getActivity(), app);
                                Bitmap bitmap = DrawableHelper.getBitmap(drawable);
                                boolean requested = mDatabase.isRequested(activity);
                                Request request = new Request(
                                        bitmap,
                                        name,
                                        app.activityInfo.packageName,
                                        activity,
                                        requested);
                                requests.add(request);
                            }
                        }
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
                    mFab.show();
                    mFab.setVisibility(View.VISIBLE);
                    mAdapter = new RequestAdapter(getActivity(), requests);
                    mRequestList.setAdapter(mAdapter);

                    if (!PermissionHelper.isPermissionStorageGranted(getActivity()))
                        PermissionHelper.requestStoragePermission(getActivity(),
                                PermissionHelper.PERMISSION_STORAGE);
                } else {
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
            StringBuilder activity;
            String zipFile;
            String productId = "";
            String orderId = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sb = new StringBuilder();
                activity = new StringBuilder();

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
                        sb.append(getDeviceInfo());

                        if (Preferences.getPreferences(getActivity()).isPremiumRequest()) {
                            if (billingProcessor == null) return false;
                            else {
                                TransactionDetails details = billingProcessor.getPurchaseTransactionDetails(
                                        Preferences.getPreferences(getActivity()).getPremiumRequestProductId());
                                if (details != null) {
                                    orderId = details.orderId;
                                    productId = details.productId;
                                    sb.append("Order ID : ").append(details.orderId)
                                            .append("\nProduct ID : ").append(details.productId)
                                            .append("\n");
                                }
                            }
                        }

                        List<Integer> selectedItems = mAdapter.getSelectedItems();
                        File fileDir = new File(mDirectory.toString() + "/" + "appfilter.xml");

                        Writer out = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(fileDir), "UTF8"));
                        for (Integer selectedItem : selectedItems) {
                            Request item = mAdapter.getRequest(selectedItem);
                            mDatabase.addRequest(item);
                            mAdapter.setRequested(selectedItem, true);

                            String link = "https://play.google.com/store/apps/details?id=";
                            activity.append("\n\n")
                                    .append(item.getName())
                                    .append("\n")
                                    .append(item.getActivity())
                                    .append("\n")
                                    .append(link).append(item.getPackageName());

                            out.append("<!-- ").append(item.getName()).append(" -->");
                            out.append("\n");
                            out.append("<item component=\"ComponentInfo{")
                                    .append(item.getActivity())
                                    .append("}\" drawable=\"")
                                    .append(item.getName().toLowerCase().replace(" ", "_"))
                                    .append("\" />");
                            out.append("\n\n");

                            String icon = FileHelper.saveIcon(mDirectory, item.getIcon(), item.getName());
                            if (icon != null) mFiles.add(icon);
                        }

                        sb.append(activity.toString());

                        if (Preferences.getPreferences(getActivity()).isPremiumRequest()) {
                            mDatabase.addPremiumRequest(orderId, productId, activity.toString());
                        }

                        out.flush();
                        out.close();
                        mFiles.add(fileDir.toString());

                        zipFile = mDirectory.toString() + "/" + "icon_request.zip";
                        FileHelper.createZip(mFiles, zipFile);
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
                sb.setLength(0);
                activity.setLength(0);
                mFiles.clear();
            }

        }.execute();
    }

    private void rebuildPremiumRequest() {
        new AsyncTask<Void, Void, Boolean>() {

            MaterialDialog dialog;
            StringBuilder sb;
            List<Request> requests;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sb = new StringBuilder();

                MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
                builder.content(R.string.premium_request_rebuilding);
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
                        requests = mDatabase.getPremiumRequest();
                        sb.append(getDeviceInfo());

                        for (Request request : requests) {
                            sb.append("\n\nOrder ID : ").append(request.getOrderId())
                                    .append("\nProduct ID : ").append(request.getProductId())
                                    .append(request.getRequest());
                        }

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
                    if (requests.size() == 0) {
                        Toast.makeText(getActivity(), R.string.premium_request_rebuilding_empty,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String subject = "Rebuild Premium Icon Request "+
                            getActivity().getResources().getString(R.string.app_name);

                    Request request = new Request(subject, sb.toString(), "");
                    IntentChooserFragment.showIntentChooserDialog(getActivity()
                            .getSupportFragmentManager(), request);
                }
                sb.setLength(0);
                mFiles.clear();
            }

        }.execute();
    }

    @NonNull
    private String getDeviceInfo() {
        DisplayMetrics displaymetrics = getActivity().getResources().getDisplayMetrics();
        StringBuilder sb = new StringBuilder();
        final int
                height = displaymetrics.heightPixels,
                width = displaymetrics.widthPixels;
        final String
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                product = Build.PRODUCT,
                os = Build.VERSION.RELEASE;
        String appVersion = "";
        try {
            appVersion = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(Tag.LOG_TAG, Log.getStackTraceString(e));
        }

        sb.append("Manufacturer : ").append(manufacturer)
                .append("\nModel : ").append(model)
                .append("\nProduct : ").append(product)
                .append("\nScreen Resolution : ")
                .append(width).append(" x ").append(height).append(" pixels")
                .append("\nAndroid Version : ").append(os)
                .append("\nApp Version : ").append(appVersion)
                .append("\n");
        return sb.toString();
    }

}
