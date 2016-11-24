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

public class Request {

    private byte[] mBytes;
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

    public Request(byte[] bytes, String name, String packageName, String activity, boolean requested) {
        mBytes = bytes;
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

    public byte[] getIcon() {
        return mBytes;
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
