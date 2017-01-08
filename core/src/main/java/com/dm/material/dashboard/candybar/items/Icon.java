package com.dm.material.dashboard.candybar.items;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

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

public class Icon implements Parcelable {

    private final String mTitle;
    private int mRes;
    private String mPackageName;
    private List<Icon> mIcons;

    public Icon(String title, int res) {
        mTitle = title;
        mRes = res;
    }

    public Icon(String title, int res, String packageName) {
        mTitle = title;
        mRes = res;
        mPackageName = packageName;
    }

    public Icon(String title, List<Icon> icons) {
        mTitle = title;
        mIcons = icons;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getRes() {
        return mRes;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public List<Icon> getIcons() {
        return mIcons;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTitle);
        dest.writeInt(this.mRes);
        dest.writeString(this.mPackageName);
    }

    protected Icon(Parcel in) {
        this.mTitle = in.readString();
        this.mRes = in.readInt();
        this.mPackageName = in.readString();
    }

    public static final Parcelable.Creator<Icon> CREATOR = new Parcelable.Creator<Icon>() {
        @Override
        public Icon createFromParcel(Parcel source) {
            return new Icon(source);
        }

        @Override
        public Icon[] newArray(int size) {
            return new Icon[size];
        }
    };
}
