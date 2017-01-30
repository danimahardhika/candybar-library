package com.dm.material.dashboard.candybar.utils;

import java.util.ArrayList;
import java.util.Collection;

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

public class ListUtils {

    public static <T> Collection<T> intersect(Collection <? extends T> a, Collection <? extends T> b) {
        Collection <T> result = new ArrayList<>();

        for (T t : a) {
            if (b.remove(t)) result.add(t);
        }
        return result;
    }

    public static <T> Collection<T> difference(Collection <? extends T> a, Collection <? extends T> b) {
        Collection <T> result = new ArrayList<>();
        result.addAll(b);

        for (T t : a) {
            result.remove(t);
        }
        return result;
    }
}
