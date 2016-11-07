package com.dm.material.dashboard.candybar.items;

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

public class InAppBilling {

    private String mProductId;
    private String mProductName;
    private String mPrice;
    private int mProductCount;

    public InAppBilling(String productId) {
        mProductId = productId;
    }

    public InAppBilling(String price, String productId, String productName) {
        mPrice = price;
        mProductId = productId;
        mProductName = productName;
    }

    public InAppBilling(String price, String productId, String productName, int productCount) {
        mPrice = price;
        mProductId = productId;
        mProductName = productName;
        mProductCount = productCount;
    }

    public InAppBilling(String productId, int productCount) {
        mProductId = productId;
        mProductCount = productCount;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getProductId() {
        return mProductId;
    }

    public String getProductName() {
        return mProductName;
    }

    public int getProductCount() {
        return mProductCount;
    }

}
