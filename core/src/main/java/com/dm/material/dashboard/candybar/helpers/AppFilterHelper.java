package com.dm.material.dashboard.candybar.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

public class AppFilterHelper {

    public static StringBuilder loadAppFilter(@NonNull Context context) throws Exception {
        StringBuilder sb = new StringBuilder();
        AssetManager asset = context.getAssets();
        InputStream stream = asset.open("appfilter.xml");
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(stream);
        NodeList list = doc.getElementsByTagName("item");

        for (int i = 0; i<list.getLength(); i++) {
            Node nNode = list.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) nNode;
                String activity = element.getAttribute("component")
                        .replace("ComponentInfo{", "").replace("}", "");
                sb.append(activity).append(", ");
            }
        }
        return sb;
    }

}
