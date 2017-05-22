package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.utils.listeners.InAppBillingListener;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;

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

public class InAppBillingHelper implements BillingProcessor.IBillingHandler {

    private final Context mContext;

    public static final int DONATE = 0;
    public static final int PREMIUM_REQUEST = 1;

    public InAppBillingHelper(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        if (Preferences.get(mContext).getInAppBillingType()
                == InAppBillingHelper.DONATE) {
            try {
                InAppBillingListener listener = (InAppBillingListener) mContext;
                listener.onInAppBillingConsume(Preferences.get(mContext)
                        .getInAppBillingType(), productId);
            } catch (Exception ignored) {}
        } else if (Preferences.get(mContext).getInAppBillingType() ==
                InAppBillingHelper.PREMIUM_REQUEST) {
            Preferences.get(mContext).setPremiumRequest(true);
            Preferences.get(mContext).setPremiumRequestProductId(productId);
            try {
                RequestListener listener = (RequestListener) mContext;
                listener.onPremiumRequestBought();
            } catch (Exception ignored) {}
        }

        Preferences.get(mContext).setInAppBillingType(-1);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
            if (Preferences.get(mContext).getInAppBillingType()
                    == InAppBillingHelper.PREMIUM_REQUEST) {
                Preferences.get(mContext).setPremiumRequestCount(0);
                Preferences.get(mContext).setPremiumRequestTotal(0);
            }
            Preferences.get(mContext).setInAppBillingType(-1);
        } else if (errorCode == Constants.BILLING_ERROR_FAILED_TO_INITIALIZE_PURCHASE) {
            try {
                InAppBillingListener listener = (InAppBillingListener) mContext;
                listener.onInAppBillingInitialized(false);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onBillingInitialized() {
        try {
            InAppBillingListener listener = (InAppBillingListener) mContext;
            listener.onInAppBillingInitialized(true);
        } catch (Exception ignored) {}
    }

    public static class Property {

        public final boolean licenseChecker;
        public final byte[] salt;
        public final String licenseKey;
        public final String[] donationProductsId;
        public final String[] premiumRequestProductsId;
        public final int[] premiumRequestProductsCount;

        public Property(boolean licenseChecker, byte[] salt, String licenseKey, String[] donationProductsId,
                        String[] premiumRequestProductsId, int[] premiumRequestProductsCount) {
            this.licenseChecker = licenseChecker;
            this.salt = salt;
            this.licenseKey = licenseKey;
            this.donationProductsId = donationProductsId;
            this.premiumRequestProductsId = premiumRequestProductsId;
            this.premiumRequestProductsCount = premiumRequestProductsCount;
        }
    }
}
