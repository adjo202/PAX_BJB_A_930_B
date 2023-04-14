/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-5-22
 * Module Author: Li Liang
 * Description:
 *
 * ============================================================================
 */
package com.pax.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ATransaction;
import com.pax.pay.app.quickclick.MenuQuickClickProtection;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.up.bjb.R;

import java.util.List;

/**
 * Adapter for {@link ListMenu}
 * <br/>
 * Created by liliang on 2017/5/4.
 */

public class ListMenuAdapter extends RecyclerView.Adapter<ListMenuAdapter.ViewHolder> {

    private Context mContext;

    private List<ListItem> mListItems;

    public ListMenuAdapter(Context context, List<ListItem> listItems) {
        if (context == null) {
            throw new IllegalArgumentException("Argument context can't be null in ListMenuAdapter");
        }
        mContext = context;
        mListItems = listItems;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.listImg.setImageResource(mListItems.get(position).getIcon());
        holder.listText.setText(mListItems.get(position).getName());
    }

    @Override
    public int getItemCount(){
        if(mListItems == null){
            return 0;
        } else {
            return mListItems.size();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_menu_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                MenuQuickClickProtection menuQuickClickProtection = MenuQuickClickProtection.getInstance();
                if (menuQuickClickProtection.isStarted()) {
                    return;
                }
                menuQuickClickProtection.start();
                process(mListItems.get(holder.getLayoutPosition()));
            }
        });

        return holder;
    }

    /**
     * Process different action for each menu item when an item is clicked.
     *
     * @param item The item that was clicked.
     */
    private void process(ListMenuAdapter.ListItem item) {
        Class<?> clazz = item.getActivity();
        if (clazz != null) {
            Intent intent = new Intent(mContext, clazz);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), item.getName());
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            intent.putExtras(bundle);
            mContext.startActivity(intent);

            return;
        }

        ATransaction trans = item.getTrans();
        if (trans != null) {
            trans.execute();
            return;
        }

        AAction action = item.getAction();
        if (action != null) {
            action.execute();
            return;
        }

    }

    private Integer getViewIcon(int position) {
        Integer resId = 0;
        ListItem holder = mListItems.get(position);
        resId = holder.getIcon();
        return resId;
    }

    private String getViewText(int position) {
        String result = null;
        ListItem holder = mListItems.get(position);
        result = holder.getName();
        return result;
    }

    public static class ListItem {

        private String name;
        private int icon;
        private ATransaction trans;
        private Class<?> activity;
        private AAction action;
        private Intent intent;

        public ListItem(String name, int icon, ATransaction trans) {
            this.name = name;
            this.icon = icon;
            this.trans = trans;
        }

        public ListItem(String name, int icon, Class<?> act) {
            this.name = name;
            this.icon = icon;
            this.activity = act;
        }

        public ListItem(String name, int icon, AAction action) {
            this.name = name;
            this.icon = icon;
            this.action = action;
        }

        public ListItem(String name, int icon, Intent intent) {
            this.name = name;
            this.icon = icon;
            this.intent = intent;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public ATransaction getTrans() {
            return trans;
        }

        public void setTrans(ATransaction trans) {
            this.trans = trans;
        }

        public Class<?> getActivity() {
            return activity;
        }

        public void setActivity(Class<?> act) {
            this.activity = act;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AAction getAction() {
            return action;
        }

        public void setAction(AAction action) {
            this.action = action;
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        public Intent getIntent() {
            return intent;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView listImg;
        TextView  listText;
        public ViewHolder(View view){
            super(view);
            listImg = (ImageView)view.findViewById(R.id.iv_item);
            listText = (TextView)view.findViewById(R.id.tv_item);
        }
    }

}
