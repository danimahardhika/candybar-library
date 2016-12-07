package com.dm.material.dashboard.candybar.utils;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import com.dm.material.dashboard.candybar.items.Icon;

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

public class SparseArrayUtils {

    private SparseArrayCompat<Icon> mIcons;
    private int mSize;

    public void sort(SparseArrayCompat<Icon> icons){
        mIcons = icons;
        mSize = mIcons.size();
        quickSort(0, mSize - 1);
    }

    private void quickSort(int low, int high) {
        if (mSize > 0) {
            int i = low, j = high;
            String pivot = mIcons.get(low + (high - low)/2).getTitle();

            while (i <= j) {
                while (compare(mIcons.get(i).getTitle(), pivot) < 0) {
                    i++;
                }
                while (compare(mIcons.get(j).getTitle(), pivot) > 0) {
                    j--;
                }

                if (i <= j) {
                    exchange(i, j);
                    i++;
                    j--;
                }
            }
            if (low < j)
                quickSort(low, j);
            if (i < high)
                quickSort(i, high);
        }
    }

    private void exchange(int i, int j) {
        Icon icon = mIcons.get(i);
        mIcons.setValueAt(i, mIcons.get(j));
        mIcons.setValueAt(j, icon);
    }

    /*
     * The Alphanum Algorithm is an improved sorting algorithm for strings
     * containing numbers.  Instead of sorting numbers in ASCII order like
     * a standard sort, this algorithm sorts numbers in numeric order.
     *
     * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com
     *
     *
     * This library is free software; you can redistribute it and/or
     * modify it under the terms of the GNU Lesser General Public
     * License as published by the Free Software Foundation; either
     * version 2.1 of the License, or any later version.
     *
     * This library is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     * Lesser General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public
     * License along with this library; if not, write to the Free Software
     * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
     *
     */

    private int compare(Object o1, Object o2) {
        if (!(o1 instanceof String) || !(o2 instanceof String)) {
            return 0;
        }
        String s1 = (String)o1;
        String s2 = (String)o2;

        int thisMarker = 0;
        int thatMarker = 0;
        int s1Length = s1.length();
        int s2Length = s2.length();

        while (thisMarker < s1Length && thatMarker < s2Length) {
            String thisChunk = getChunk(s1, s1Length, thisMarker);
            thisMarker += thisChunk.length();

            String thatChunk = getChunk(s2, s2Length, thatMarker);
            thatMarker += thatChunk.length();

            int result;
            if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
                int thisChunkLength = thisChunk.length();
                result = thisChunkLength - thatChunk.length();
                if (result == 0) {
                    for (int i = 0; i < thisChunkLength; i++) {
                        result = thisChunk.charAt(i) - thatChunk.charAt(i);
                        if (result != 0) {
                            return result;
                        }
                    }
                }
            } else {
                result = thisChunk.compareTo(thatChunk);
            }

            if (result != 0)
                return result;
        }
        return s1Length - s2Length;
    }

    private boolean isDigit(char ch) {
        return ch >= 48 && ch <= 57;
    }

    @NonNull
    private String getChunk(String s, int length, int marker) {
        StringBuilder chunk = new StringBuilder();
        char c = s.charAt(marker);
        chunk.append(c);
        marker++;
        if (isDigit(c)) {
            while (marker < length) {
                c = s.charAt(marker);
                if (!isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        } else {
            while (marker < length) {
                c = s.charAt(marker);
                if (isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        }
        return chunk.toString();
    }

}
