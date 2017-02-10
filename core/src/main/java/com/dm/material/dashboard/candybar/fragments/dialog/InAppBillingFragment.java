package com.dm.material.dashboard.candybar.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.adapters.InAppBillingAdapter;
import com.dm.material.dashboard.candybar.helpers.InAppBillingHelper;
import com.dm.material.dashboard.candybar.items.InAppBilling;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.Tag;
import com.dm.material.dashboard.candybar.utils.listeners.InAppBillingListener;

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

public class InAppBillingFragment extends DialogFragment {

    private static BillingProcessor mBillingProcessor;

    private static final String TYPE = "type";
    private static final String KEY = "key";
    private static final String PRODUCT_ID = "product_id";
    private static final String PRODUCT_COUNT = "product_count";

    private static final String TAG = "candybar.dialog.inapp.billing";

    private static InAppBillingFragment newInstance(int type, String key, String[] productId, int[] productCount) {
        InAppBillingFragment fragment = new InAppBillingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        bundle.putString(KEY, key);
        bundle.putStringArray(PRODUCT_ID, productId);
        if (productCount != null)
            bundle.putIntArray(PRODUCT_COUNT, productCount);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showInAppBillingDialog(@NonNull FragmentManager fm, BillingProcessor billingProcessor,
                                              int type, @NonNull String key, @NonNull String[] productId,
                                              int[] productCount) {
        mBillingProcessor = billingProcessor;
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = InAppBillingFragment.newInstance(type, key, productId, productCount);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {}
    }

    private ListView mInAppList;
    private ProgressBar mProgress;

    private int mType;
    private String mKey;
    private String[] mProductsId;
    private int[] mProductsCount;

    private InAppBillingAdapter mAdapter;
    private AsyncTask<Void, Void, Boolean> mLoadInAppProducts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(TYPE);
        mKey = getArguments().getString(KEY);
        mProductsId = getArguments().getStringArray(PRODUCT_ID);
        mProductsCount = getArguments().getIntArray(PRODUCT_COUNT);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_inapp_dialog, false)
                .positiveText(mType == InAppBillingHelper.DONATE ?
                        R.string.donate : R.string.premium_request_buy)
                .negativeText(R.string.close)
                .onPositive((dialog, which) -> {
                    if (mLoadInAppProducts == null) {
                        try {
                            InAppBillingListener listener = (InAppBillingListener) getActivity();
                            listener.OnInAppBillingSelected(
                                    mType, mAdapter.getSelectedProduct());
                        } catch (Exception ignored) {}
                        dismiss();
                    }
                })
                .onNegative((dialog, which) ->
                        Preferences.getPreferences(getActivity()).setInAppBillingType(-1));
        MaterialDialog dialog = builder.build();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        setCancelable(false);

        TextView header = (TextView) dialog.findViewById(R.id.header);
        mInAppList = (ListView) dialog.findViewById(R.id.inapp_list);
        mProgress = (ProgressBar) dialog.findViewById(R.id.progress);

        header.setText(mType == InAppBillingHelper.DONATE ?
                R.string.navigation_view_donate : R.string.premium_request);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mType = savedInstanceState.getInt(TYPE);
            mKey = savedInstanceState.getString(KEY);
            mProductsId = savedInstanceState.getStringArray(PRODUCT_ID);
            mProductsCount = savedInstanceState.getIntArray(PRODUCT_COUNT);
        }
        loadInAppProducts();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TYPE, mType);
        outState.putString(KEY, mKey);
        outState.putStringArray(PRODUCT_ID, mProductsId);
        outState.putIntArray(PRODUCT_COUNT, mProductsCount);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mBillingProcessor = null;
        if (mLoadInAppProducts != null) mLoadInAppProducts.cancel(true);
        super.onDismiss(dialog);
    }

    private void loadInAppProducts() {
        mLoadInAppProducts = new AsyncTask<Void, Void, Boolean>() {

            InAppBilling[] inAppBillings;
            boolean isBillingNotReady = false;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgress.setVisibility(View.VISIBLE);
                inAppBillings = new InAppBilling[mProductsId.length];
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                while (!isCancelled()) {
                    try {
                        Thread.sleep(1);
                        if (mBillingProcessor == null) {
                            isBillingNotReady = true;
                            return false;
                        }

                        for (int i = 0; i < mProductsId.length; i++) {
                            SkuDetails product = mBillingProcessor
                                    .getPurchaseListingDetails(mProductsId[i]);
                            if (product != null) {
                                InAppBilling inAppBilling;
                                String title = product.title.substring(0, product.title.lastIndexOf("("));
                                if (mProductsCount != null) {
                                    inAppBilling = new InAppBilling(product.priceText, mProductsId[i],
                                            title, mProductsCount[i]);
                                } else {
                                    inAppBilling = new InAppBilling(product.priceText, mProductsId[i],
                                            title);
                                }
                                inAppBillings[i] = inAppBilling;
                            } else {
                                if (i == mProductsId.length - 1)
                                    return false;
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
                    mAdapter = new InAppBillingAdapter(getActivity(), inAppBillings);
                    mInAppList.setAdapter(mAdapter);
                } else {
                    dismiss();
                    Preferences.getPreferences(getActivity()).setInAppBillingType(-1);
                    if (!isBillingNotReady)
                        Toast.makeText(getActivity(), R.string.billing_load_product_failed,
                                Toast.LENGTH_LONG).show();
                }
                mLoadInAppProducts = null;
            }

        }.execute();
    }

}
