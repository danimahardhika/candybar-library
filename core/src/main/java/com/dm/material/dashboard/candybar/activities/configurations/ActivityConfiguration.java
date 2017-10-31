package com.dm.material.dashboard.candybar.activities.configurations;

import android.support.annotation.NonNull;

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

public class ActivityConfiguration {

    private boolean mIsLicenseCheckerEnabled;
    private byte[] mRandomString;
    private String mLicenseKey;
    private String[] mDonationProductsId;
    private String[] mPremiumRequestProductsId;
    private int[] mPremiumRequestProductsCount;

    public ActivityConfiguration setLicenseCheckerEnabled(boolean enabled) {
        mIsLicenseCheckerEnabled = enabled;
        return this;
    }

    public ActivityConfiguration setRandomString(@NonNull byte[] randomString) {
        mRandomString = randomString;
        return this;
    }

    public ActivityConfiguration setLicenseKey(@NonNull String licenseKey) {
        mLicenseKey = licenseKey;
        return this;
    }

    public ActivityConfiguration setDonationProductsId(@NonNull String[] productsId) {
        mDonationProductsId = productsId;
        return this;
    }

    public ActivityConfiguration setPremiumRequestProducts(@NonNull String[] ids, @NonNull int[] counts) {
        mPremiumRequestProductsId = ids;
        mPremiumRequestProductsCount = counts;
        return this;
    }

    public boolean isLicenseCheckerEnabled() {
        return mIsLicenseCheckerEnabled;
    }

    public byte[] getRandomString() {
        return mRandomString;
    }

    public String getLicenseKey() {
        return mLicenseKey;
    }

    public String[] getDonationProductsId() {
        return mDonationProductsId;
    }

    public String[] getPremiumRequestProductsId() {
        return mPremiumRequestProductsId;
    }

    public int[] getPremiumRequestProductsCount() {
        return mPremiumRequestProductsCount;
    }
}
