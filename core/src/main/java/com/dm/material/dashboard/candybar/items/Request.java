package com.dm.material.dashboard.candybar.items;

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

import android.content.ComponentName;
import android.support.annotation.Nullable;

public class Request {

    private String mName;
    private String mActivity;
    private String mPackageName;
    private String mOrderId;
    private String mProductId;
    private String mRequestedOn;
    private boolean mRequested;

    public Request(String name, String packageName, String activity, boolean requested) {
        mName = name;
        mPackageName = packageName;
        mActivity = activity;
        mRequested = requested;
    }

    public Request(String orderId, String productId, String name, String activity) {
        mOrderId = orderId;
        mProductId = productId;
        mName = name;
        mActivity = activity;
    }

    public String getName() {
        return mName;
    }

    public String getPackageName() {
        if (mPackageName == null) {
            return mActivity.substring(0, mActivity.lastIndexOf("/"));
        }
        return mPackageName;
    }

    public String getActivity() {
        return mActivity;
    }

    public boolean isRequested() {
        return mRequested;
    }

    public void setRequested(boolean requested) {
        mRequested = requested;
    }

    public String getOrderId() {
        return mOrderId;
    }

    public String getProductId() {
        return mProductId;
    }

    public String getRequestedOn() {
        return mRequestedOn;
    }

    public static class Property {

        private ComponentName componentName;
        private final String orderId;
        private final String productId;

        public Property(ComponentName componentName, String orderId, String productId){
            this.componentName = componentName;
            this.orderId = orderId;
            this.productId = productId;
        }

        @Nullable
        public ComponentName getComponentName() {
            return componentName;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getProductId() {
            return productId;
        }

        public void setComponentName(ComponentName componentName) {
            this.componentName = componentName;
        }
    }
}
