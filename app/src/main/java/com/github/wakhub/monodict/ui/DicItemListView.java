/**
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
package com.github.wakhub.monodict.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.wakhub.monodict.R;
import com.github.wakhub.monodict.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: refactoring
public class DicItemListView extends ListView {

    public static class Data {
        public static final int WORD = 0;
        public static final int MORE = 1;
        public static final int NONE = 2;
        public static final int NORESULT = 3;
        public static final int FOOTER = 4;


        private int mDic;
        private int mMode;
        public CharSequence Index;
        public CharSequence Phone;
        public CharSequence Trans;
        public CharSequence Sample;

        public boolean LastItem = false;
        public Typeface IndexFont;
        public Typeface PhoneFont;
        public Typeface TransFont;
        public Typeface SampleFont;
        public int IndexSize;
        public int PhoneSize;
        public int TransSize;
        public int SampleSize;

        public Data(int mode, int dic) {
            mDic = dic;
            mMode = mode;
        }

        @Override
        public String toString() {
            String index = Index != null ? Index.toString() : "";
            String phone = Phone != null ? Phone.toString() : "";
            String trans = Trans != null ? Trans.toString() : "";
            String sample = Sample != null ? Sample.toString() : "";
            return String.format("index: %s\nphone: %s\ntrans: %s\nsample: %s", index, phone, trans, sample);
        }

        public String toSummaryString() {
            String all = Index.toString();
            if (Trans != null) {
                all += "\n";
                all += Trans;
            }

            if (Sample != null) {
                all += "\n";
                all += Sample;
            }

            all += "\n";
            return all;
        }

        public List<Pair<String, String>> getAdditionalLinkInfo() {
            List<Pair<String, String>> linkPairs = new ArrayList<Pair<String, String>>();

            // <→リンク> 英辞郎形式
            {
                Pattern p = Pattern.compile("<(→(.+?))>");
                Matcher m = p.matcher(Trans);

                while (m.find()) {
                    linkPairs.add(new Pair<String, String>(m.group(1), m.group(2)));
                }
            }
            // "→　" 和英辞郎形式
            {
                Pattern p = Pattern.compile("(→　(.+))");
                Matcher m = p.matcher(Trans);

                while (m.find()) {
                    linkPairs.add(new Pair<String, String>(m.group(1), m.group(2)));
                }
            }

            // "＝リンク●" 略辞郎形式
            {
                Pattern p = Pattern.compile("(＝(.+))●");
                Matcher m = p.matcher(Trans);

                while (m.find()) {
                    linkPairs.add(new Pair<String, String>(m.group(1), m.group(2)));
                }
            }
            return linkPairs;
        }

        public int getDic() {
            return mDic;
        }

        public int getMode() {
            return mMode;
        }
    }

    public interface Callback {
        void onDicviewItemClickAddToFlashcardButton(int position);

        void onDicviewItemClickSpeechButton(int position);

        void onDicviewItemClickActionButton(int position);

        void onDicviewItemActionModeSearch(String selectedText);

        void onDicviewItemActionModeSpeech(String selectedText);
    }

    Callback callback;

    private void init(Context context) {
        setSmoothScrollbarEnabled(true);
        setScrollingCacheEnabled(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setFastScrollEnabled(true);
        setCacheColorHint(Color.WHITE);
    }

    public DicItemListView(Context context) {
        super(context);
        init(context);
    }

    public DicItemListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DicItemListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setCallback(Callback cb) {
        callback = cb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        requestFocus();
        return super.onTouchEvent(ev);
    }

    static class ActionModeCallback implements ActionMode.Callback {

        private TextView textView;
        private Context context;

        ActionModeCallback(Context context, TextView textView) {
            this.textView = textView;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            }

            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.main_action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            ViewParent parentView = ViewUtils.findParentView(textView, DicItemListView.class);
            int selectionStart = textView.getSelectionStart();
            int selectionEnd = textView.getSelectionEnd();
            int min = Math.max(0, Math.min(selectionStart, selectionEnd));
            int max = Math.max(0, Math.max(selectionStart, selectionEnd));
            String selectedText = textView.getText().subSequence(min, max).toString();

            if (menuItem.getItemId() == R.id.action_search) {
                ((DicItemListView) parentView).callback.onDicviewItemActionModeSearch(selectedText);
                actionMode.finish();
                return true;
            }
            if (menuItem.getItemId() == R.id.action_speech) {
                ((DicItemListView) parentView).callback.onDicviewItemActionModeSpeech(selectedText);
                actionMode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
        }
    }

    public static class ResultAdapter extends ArrayAdapter<Data> {

        static class ViewHolder {
            TextView Index;
            TextView Phone;
            TextView Trans;
            ImageButton addToFlashcardButton;
            ImageButton speechButton;
            ImageButton actionButton;
            TextView Sample;
            Button moreButton;
            View hr;
        }


        public ResultAdapter(Context context, int resource, int textViewResourceId, ArrayList<Data> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            ViewHolder holder;
            if (convertView != null && (convertView instanceof LinearLayout)) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = inflate(getContext(), R.layout.list_item_dic, null);
                Context context = getContext();

                holder = new ViewHolder();

                holder.Index = (TextView) view.findViewById(R.id.ListIndex);
                holder.Index.setCustomSelectionActionModeCallback(new ActionModeCallback(context, holder.Index));

                holder.Phone = (TextView) view.findViewById(R.id.ListPhone);
                holder.Phone.setCustomSelectionActionModeCallback(new ActionModeCallback(context, holder.Phone));

                holder.Trans = (TextView) view.findViewById(R.id.ListTrans);
                holder.Trans.setCustomSelectionActionModeCallback(new ActionModeCallback(context, holder.Trans));

                holder.Sample = (TextView) view.findViewById(R.id.ListSample);
                holder.Sample.setCustomSelectionActionModeCallback(new ActionModeCallback(context, holder.Sample));

                holder.moreButton = (Button) view.findViewById(R.id.ListMoreButton);

                holder.addToFlashcardButton = (ImageButton) view.findViewById(R.id.add_to_flashcard_button);
                holder.addToFlashcardButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View rowView = (View) ViewUtils.findParentView(view, R.id.root);
                        DicItemListView listView = (DicItemListView) rowView.getParent();
                        listView.callback.onDicviewItemClickAddToFlashcardButton(listView.getPositionForView(rowView));
                    }
                });
                holder.speechButton = (ImageButton) view.findViewById(R.id.speech_button);
                holder.speechButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View rowView = (View) ViewUtils.findParentView(view, R.id.root);
                        DicItemListView listView = (DicItemListView) rowView.getParent();
                        listView.callback.onDicviewItemClickSpeechButton(listView.getPositionForView(rowView));
                    }
                });
                holder.actionButton = (ImageButton) view.findViewById(R.id.action_button);
                holder.actionButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View rowView = (View) ViewUtils.findParentView(view, R.id.root);
                        DicItemListView listView = (DicItemListView) rowView.getParent();
                        listView.callback.onDicviewItemClickActionButton(listView.getPositionForView(rowView));
                    }
                });
                holder.hr = view.findViewById(R.id.hr);

                holder.Index.setTextColor(Color.BLACK);
                holder.Phone.setTextColor(Color.BLACK);
                holder.Trans.setTextColor(Color.BLACK);
                holder.Sample.setTextColor(Color.BLACK);

                view.setTag(holder);
            }
            Data d = getItem(position);

            holder.addToFlashcardButton.setVisibility(View.GONE);
            holder.speechButton.setVisibility(View.GONE);
            holder.actionButton.setVisibility(View.GONE);
            holder.hr.setVisibility(View.VISIBLE);
            TextView header = (TextView) view.findViewById(R.id.header);
            header.setVisibility(View.GONE);
            View content = view.findViewById(R.id.content);
            content.setVisibility(View.VISIBLE);

            setItem(holder.Index, d.Index, d.IndexFont, 18);

            switch (d.getMode()) {
                case Data.WORD:
                    setItem(holder.Index, d.Index, d.IndexFont, 24);
                    setItem(holder.Phone, d.Phone, d.PhoneFont, 0);
                    setItem(holder.Trans, d.Trans, d.TransFont, 0);
                    setItem(holder.Sample, d.Sample, d.SampleFont, 0);
                    holder.addToFlashcardButton.setVisibility(View.VISIBLE);
                    holder.speechButton.setVisibility(View.VISIBLE);
                    holder.actionButton.setVisibility(View.VISIBLE);
                    holder.moreButton.setVisibility(View.GONE);
                    if (d.LastItem) {
                        holder.hr.setVisibility(View.GONE);
                    }
                    break;
                case Data.MORE:
                    holder.Index.setVisibility(View.GONE);
                    holder.Phone.setVisibility(View.GONE);
                    holder.Trans.setVisibility(View.GONE);
                    holder.Sample.setVisibility(View.GONE);
                    holder.moreButton.setVisibility(View.VISIBLE);
                    holder.moreButton.setText(d.Index);
                    break;
                case Data.NORESULT:
                case Data.NONE:
                    setItem(holder.Index, d.Index, d.IndexFont, 0);
                    holder.Phone.setVisibility(View.GONE);
                    holder.Trans.setVisibility(View.GONE);
                    holder.Sample.setVisibility(View.GONE);
                    holder.moreButton.setVisibility(View.GONE);
                    break;
                case Data.FOOTER:
                    header.setVisibility(View.VISIBLE);
                    header.setText(d.Index);
                    content.setVisibility(View.GONE);

                    setItem(holder.Index, d.Index, d.IndexFont, 14);
                    holder.Index.setVisibility(View.VISIBLE);
                    holder.Phone.setVisibility(View.GONE);
                    holder.Trans.setVisibility(View.GONE);
                    holder.Sample.setVisibility(View.GONE);
                    holder.moreButton.setVisibility(View.GONE);
                    holder.hr.setVisibility(View.INVISIBLE);
                    break;
                default:
                    holder.Index.setVisibility(View.GONE);
                    holder.Phone.setVisibility(View.GONE);
                    holder.Trans.setVisibility(View.GONE);
                    holder.Sample.setVisibility(View.GONE);
                    holder.moreButton.setVisibility(View.GONE);
            }
            return view;
        }

        private void setItem(TextView tv, CharSequence str, Typeface tf, int size) {
            if (str == null || str.length() == 0) {
                tv.setVisibility(View.GONE);
            } else {
                tv.setVisibility(View.VISIBLE);
                tv.setText(str);
                if (size > 10) {
                    tv.setTextSize(size);
                }
            }
        }
    }
}
