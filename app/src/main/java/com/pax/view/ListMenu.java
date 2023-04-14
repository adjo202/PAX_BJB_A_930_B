/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-5-22 11:47
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ATransaction;
import com.pax.up.bjb.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Vertical scrolling menu.
 *
 * <br/><br/>Created by liliang on 2017/5/4.
 */

public class ListMenu extends LinearLayout {

    private List<ListMenuAdapter.ListItem> mItemList;
    private RecyclerView mListView;

    public ListMenu(Context context, List<ListMenuAdapter.ListItem> itemList) {
        super(context);
        mItemList = itemList;
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_menu, this, true);
        mListView = (RecyclerView) view.findViewById(R.id.menu_list);
        mListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mListView.setAdapter(new ListMenuAdapter(getContext(), mItemList));
        mListView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL, R.drawable.divider));
    }

    public static class Builder {
        private Context mContext;
        private List<ListMenuAdapter.ListItem> itemList;
        private ListMenu mListMenu;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        /**
         * Add items related with transactions.
         *
         * @param title The title of a menu item.
         * @param icon The icon of a menu item.
         * @param trans The transaction an item would process.
         * @return Return the builder.
         */

        public ListMenu.Builder addTransItem(String title, int icon, ATransaction trans) {
            if (itemList == null) {
                itemList = new ArrayList<ListMenuAdapter.ListItem>();
            }
            itemList.add(new ListMenuAdapter.ListItem(title, icon, trans));
            return this;
        }

        /**
         * Add menu items that link to another activity.
         *
         * @param title The title of a menu item.
         * @param icon The icon of a menu item.
         * @param act The activity to open when the item is clicked.
         * @return
         */
        public ListMenu.Builder addMenuItem(String title, int icon, Class<?> act) {
            if (itemList == null) {
                itemList = new ArrayList<ListMenuAdapter.ListItem>();
            }
            itemList.add(new ListMenuAdapter.ListItem(title, icon, act));
            return this;
        }

        /**
         * Add menu items with actions.
         *
         * @param title The title of a menu item.
         * @param icon The icon of a menu item.
         * @param action The action to be executed when clicked.
         * @return
         */
        public ListMenu.Builder addActionItem(String title, int icon, AAction action) {
            if (itemList == null) {
                itemList = new ArrayList<ListMenuAdapter.ListItem>();
            }
            itemList.add(new ListMenuAdapter.ListItem(title, icon, action));
            return this;

        }

        /**
         * Create n vertical scrolling menu.
         *
         * @return Return n vertical scrolling menu.
         */
        public ListMenu create() {
            mListMenu = new ListMenu(mContext, itemList);
            return mListMenu;
        }

    }
}
