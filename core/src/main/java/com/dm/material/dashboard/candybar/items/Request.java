package com.dm.material.dashboard.candybar.items;

import android.graphics.Bitmap;

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

public class Request {

    private Bitmap mIcon;
    private String mName;
    private String mActivity;
    private String mPackageName;
    private String mSubject;
    private String mText;
    private String mStream;
    private String mOrderId;
    private String mProductId;
    private String mRequest;
    private String mRequestedOn;
    private boolean mRequested;
    private int mRequestCount;

    public Request(Bitmap icon, String name, String packageName, String activity, boolean requested) {
        mIcon = icon;
        mName = name;
        mPackageName = packageName;
        mActivity = activity;
        mRequested = requested;
    }

    public Request(String subject, String text, String stream) {
        mSubject = subject;
        mText = text;
        mStream = stream;
    }

    public Request(String subject, String text, String stream, int requestCount) {
        mSubject = subject;
        mText = text;
        mStream = stream;
        mRequestCount = requestCount;
    }

    public Request(String orderId, String productId, String request, String requestedOn) {
        mOrderId = orderId;
        mProductId = productId;
        mRequest = request;
        mRequestedOn = requestedOn;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public String getName() {
        return mName;
    }

    public String getPackageName() {
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

    public String getSubject() {
        return mSubject;
    }

    public String getText() {
        return mText;
    }

    public String getStream() {
        return mStream;
    }

    public int getRequestCount() {
        return mRequestCount;
    }

    public String getOrderId() {
        return mOrderId;
    }

    public String getProductId() {
        return mProductId;
    }

    public String getRequest() {
        return mRequest;
    }

    public String getRequestedOn() {
        return mRequestedOn;
    }

}
