/*
 * Copyright (c) 2015, 张涛.
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
package com.kymjs.core.bitmap.client;

import android.graphics.drawable.Drawable;

import com.kymjs.rxvolley.client.RequestConfig;
import com.kymjs.rxvolley.toolbox.HttpParamsEntry;

import java.util.ArrayList;

/**
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public class BitmapRequestConfig extends RequestConfig {

    public static int DEF_WIDTH_HEIGHT = -100;

    public int maxWidth = DEF_WIDTH_HEIGHT;
    public int maxHeight = DEF_WIDTH_HEIGHT;

    public Drawable loadDrawable;
    public Drawable errorDrawable;
    public int loadRes;
    public int errorRes;

    private final ArrayList<HttpParamsEntry> mHeaders = new ArrayList<>();

    public ArrayList<HttpParamsEntry> getHeaders() {
        return mHeaders;
    }

    public void putHeader(String k, String v) {
        mHeaders.add(new HttpParamsEntry(k, v));
    }
}
