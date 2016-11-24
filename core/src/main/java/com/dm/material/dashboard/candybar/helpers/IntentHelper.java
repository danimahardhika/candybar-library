package com.dm.material.dashboard.candybar.helpers;

import android.content.Intent;

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

public class IntentHelper {

    public static final int ACTION_DEFAULT = 0;
    public static final int ICON_PICKER = 1;
    public static final int IMAGE_PICKER = 2;

    public static int sAction = ACTION_DEFAULT;

    private static final String ACTION_ADW_PICK_ICON = "org.adw.launcher.icons.ACTION_PICK_ICON";
    private static final String ACTION_TURBO_PICK_ICON = "com.phonemetra.turbo.launcher.icons.ACTION_PICK_ICON";
    private static final String ACTION_NOVA_LAUNCHER = "com.novalauncher.THEME";

    public static int getAction(Intent intent) {
        if (intent == null) return ACTION_DEFAULT;
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_ADW_PICK_ICON :
                case ACTION_TURBO_PICK_ICON :
                case ACTION_NOVA_LAUNCHER :
                    return ICON_PICKER;
                case Intent.ACTION_PICK :
                case Intent.ACTION_GET_CONTENT :
                    return IMAGE_PICKER;
            }
        }
        return ACTION_DEFAULT;
    }

}
