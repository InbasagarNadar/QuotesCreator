package com.dartt.yello;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FaqActivity extends AppCompatActivity {


    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        expandableListView = findViewById(R.id.expandable_list_view);
        expandableListAdapter = new CustomExpandableListAdapter(this);
        expandableListView.setAdapter(expandableListAdapter);
    }

    public void onCancelClick(View view) {
        finish();
    }


    class CustomExpandableListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private String[] questions;
        private String[] answers;


        public CustomExpandableListAdapter(Context context) {
            this.context = context;
            questions = getResources().getStringArray(R.array.faq_questions);
            answers = getResources().getStringArray(R.array.faq_answers);
        }

        @Override
        public Object getChild(int listPosition, int expandedListPosition) {
            return answers[listPosition];

        }

        @Override
        public long getChildId(int listPosition, int expandedListPosition) {
            return expandedListPosition;
        }

        @Override
        public View getChildView(int listPosition, final int expandedListPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final String expandedListText = (String) getChild(listPosition, expandedListPosition);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.faq_list_item, null);
            }
            TextView expandedListTextView = convertView
                    .findViewById(R.id.expandedListItem);
            expandedListTextView.setText(expandedListText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int listPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int listPosition) {
            return this.questions[listPosition];
        }

        @Override
        public int getGroupCount() {
            return this.questions.length;
        }

        @Override
        public long getGroupId(int listPosition) {
            return listPosition;
        }

        @Override
        public View getGroupView(int listPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String listTitle = (String) getGroup(listPosition);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context.
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.faq_list_group, null);
            }
            TextView listTitleTextView = convertView
                    .findViewById(R.id.listTitle);
            listTitleTextView.setTypeface(null, Typeface.BOLD);
            listTitleTextView.setText(listTitle);
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int listPosition, int expandedListPosition) {
            return true;
        }
    }

}



