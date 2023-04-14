package com.pax.pay.menu;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.pay.emv.EmvAid;
import com.pax.up.bjb.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangsh on 2017/4/23.
 * Modified by tuxq on 2017/5/16.
 */

public class QueryAppListActivity extends Activity {

    private List<EmvAid> emvAidList;

    private MyAdapter adapter;
    private List<GroupBean> list;

    private ExpandableListView expandableListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.activity_query_applist_layout);
        expandableListView = (ExpandableListView) findViewById(R.id.queryListView);
        ImageView backArrowView = (ImageView) findViewById(R.id.header_back_arrow);
        backArrowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_applisttitle);
        tvTitle.setText("Query App List");
        adapter = new MyAdapter(list, this);
        expandableListView.setAdapter(adapter);
    }

    private void initData(){
        emvAidList = EmvAid.readAllAid();
        list = new ArrayList<>();
        ChildBean cb;
        GroupBean gb;
        for(int i = 0; i < emvAidList.size(); i++){
            EmvAid emvAid = emvAidList.get(i);
            List<ChildBean> list1 = new ArrayList<>();
            cb = new ChildBean(emvAid.getTacDenial(), emvAid.getTacOnline(), emvAid.getTacDefualt(), emvAid.getVersion());
            list1.add(cb);
            gb = new GroupBean(emvAid.getAid(), list1);
            list.add(gb);
        }
    }

    //ΪExpandableListView
    public class MyAdapter extends BaseExpandableListAdapter {

        private List<GroupBean> list;
        private Context context;

        public MyAdapter(List<GroupBean> list, Context context) {
            this.list = list;
            this.context = context;
        }

        public MyAdapter() {
        }

        @Override
        public int getGroupCount() {
            return list.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return list.get(groupPosition).getChildren().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return list.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return list.get(groupPosition).getChildren().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            GroupHolder holder;
            if (convertView == null) {
                holder = new GroupHolder();
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.applist_group, null);
                holder.title = (TextView) convertView
                        .findViewById(R.id.tv_group);
                convertView.setTag(holder); //Adapter 有个getView方法，可以使用setTag把查找的view缓存起来方便多次重用
            } else {
                holder = (GroupHolder) convertView.getTag();
            }
            holder.title.setText(list.get(groupPosition).getGroupName());
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder holder;
            if (convertView == null) {
                holder = new ChildHolder();
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.applist_child, null);
                holder.tacDenial = (TextView) convertView.findViewById(R.id.tv_child1);
                holder.tacOnline = (TextView) convertView.findViewById(R.id.tv_child2);
                holder.tacDefault = (TextView) convertView.findViewById(R.id.tv_child3);
                holder.version = (TextView) convertView.findViewById(R.id.tv_child4);
                convertView.setTag(holder);
            } else {
                holder = (ChildHolder) convertView.getTag();
            }
            ChildBean cb = list.get(groupPosition).getChildren().get(childPosition);
            holder.tacDenial.setText(cb.getTacDenial());
            holder.tacOnline.setText(cb.getTacOnline());
            holder.tacDefault.setText(cb.getTacDefault());
            holder.version.setText(cb.getVersion());
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        class GroupHolder {
            TextView title;
            ImageView iv;
        }

        class ChildHolder {
            TextView tacDenial;
            TextView tacOnline;
            TextView tacDefault;
            TextView version;
        }
    }
    class ChildBean {

        private String tacDenial;
        private String tacOnline;
        private String tacDefault;
        private String version;

        public ChildBean() {
        }

        public ChildBean(String tacDenial, String tacOnline, String tacDefault, String version) {
            this.tacDenial = tacDenial;
            this.tacOnline = tacOnline;
            this.tacDefault = tacDefault;
            this.version = version;
        }

        public String getTacDenial() {
            return "TacDenial : " + tacDenial;
        }

        public void setTacDenial(String tacDenial) {
            this.tacDenial = tacDenial;
        }

        public String getTacOnline() {
            return "TacOnline : " + tacOnline;
        }

        public void setTacOnline(String tacOnline) {
            this.tacOnline = tacOnline;
        }

        public String getTacDefault() {
            return "TacDefault : " + tacDefault;
        }

        public void setTacDefault(String tacDefault) {
            this.tacDefault = tacDefault;
        }

        public String getVersion() {
            return "Version : " + version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    class GroupBean {

        private String groupName;
        private List<ChildBean> children;

        public GroupBean() {
        }

        public GroupBean(String groupName, List<ChildBean> children) {
            this.groupName = groupName;
            this.children = children;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<ChildBean> getChildren() {
            return children;
        }

        public void setChildren(List<ChildBean> children) {
            this.children = children;
        }

    }
}
