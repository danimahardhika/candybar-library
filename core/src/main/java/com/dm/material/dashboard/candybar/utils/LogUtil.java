package com.dm.material.dashboard.candybar.utils;

import android.util.Log;

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
}
