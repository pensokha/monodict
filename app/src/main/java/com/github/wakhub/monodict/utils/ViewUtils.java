/*
 * Copyright (C) 2014 wak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.wakhub.monodict.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by wak on 6/28/14.
 */
public class ViewUtils {

    private static final int MAX_DEPTH = 100;

    public static ViewParent findParentView(View rootView, Class findClass) {
        View currentView = rootView;
        for (int i = 0; i < MAX_DEPTH; i++) {
            ViewParent parentView = currentView.getParent();
            if (parentView == null) {
                return null;
            }
            if (findClass.isInstance(parentView)) {
                return parentView;
            }
            currentView = (View) parentView;
        }

        return null;
    }

    public static ViewParent findParentView(View rootView, int viewId) {
        View currentView = rootView;
        for (int i = 0; i < MAX_DEPTH; i++) {
            ViewParent parentView = currentView.getParent();
            if (parentView == null) {
                return null;
            }
            if (((View) parentView).getId() == viewId) {
                return parentView;
            }
            currentView = (View) parentView;
        }

        return null;
    }

    public static void addMarginBottom(View view, int marginBottom) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMargins(
                params.leftMargin,
                params.topMargin,
                params.rightMargin,
                params.bottomMargin + marginBottom);
        view.requestLayout();
    }
}
