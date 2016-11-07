package com.dm.material.dashboard.candybar.utils.listeners;

import com.dm.material.dashboard.candybar.items.InAppBilling;

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

public interface InAppBillingListener {

    void OnInAppBillingInitialized(boolean success);
    void OnRestorePurchases();
    void OnInAppBillingSelected(int type, InAppBilling product);
    void OnInAppBillingConsume(int type, String productId);
    void OnInAppBillingRequest();

}
