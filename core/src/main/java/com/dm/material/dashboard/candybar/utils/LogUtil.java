package com.dm.material.dashboard.candybar.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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

public class LogUtil {

    private static boolean sIsLoggingEnabled = false;

    private static final String TAG = "CandyBar";

    public static void setLoggingEnabled(boolean enabled) {
        LogUtil.sIsLoggingEnabled = enabled;
    }

    public static void d(String message) {
        if (LogUtil.sIsLoggingEnabled)
            Log.d(TAG, message);
    }

    public static void e(String message) {
        if (LogUtil.sIsLoggingEnabled)
            Log.e(TAG, message);
    }

    public enum Error {
        APPFILTER_NULL,
        DATABASE_ERROR,
        INSTALLED_APPS_NULL,
        ICON_REQUEST_NULL,
        ICON_REQUEST_PROPERTY_NULL,
        ICON_REQUEST_PROPERTY_COMPONENT_NULL;

        Error() {}

        public String getMessage() {
            switch (this) {
                case APPFILTER_NULL:
                    return "Error: Unable to read appfilter.xml";
                case DATABASE_ERROR:
                    return "Error: Unable to read database";
                case INSTALLED_APPS_NULL:
                    return "Error: Unable to collect installed apps";
                case ICON_REQUEST_NULL:
                    return "Error: Icon request is null";
                case ICON_REQUEST_PROPERTY_NULL:
                    return "Error: Icon request property is null";
                case ICON_REQUEST_PROPERTY_COMPONENT_NULL:
                    return "Error: Email client component is null";
                default:
                    return "Error: Unknown";
            }
        }

        public void showToast(Context context) {
            if (context == null) return;
            Toast.makeText(context, getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
