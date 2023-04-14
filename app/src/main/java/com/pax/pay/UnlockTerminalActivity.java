/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-5-22 11:47
 *  Module Author: Richard
 *  Description:
 *  ============================================================================
 */

package com.pax.pay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.pax.pay.constant.AdConstants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.operator.OperLogonActivity;
import com.pax.up.bjb.R;

import java.util.ArrayList;

import cn.bingoogolapple.bgabanner.BGABanner;

public class UnlockTerminalActivity extends BaseActivity{
    private static Intent lockerIntent = null;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //must set before setContentView
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId(){
        return  R.layout.activity_unlock_terminal;
    }

    @Override
    protected void initViews(){
        mGestureDetector = new GestureDetector(this, new LearnGestureListener());

        BGABanner banner = (BGABanner)findViewById(R.id.banner_guide_content);
        banner.setAdapter(new BGABanner.Adapter<ImageView, String>() {
            @Override
            public void fillBannerItem(BGABanner banner, ImageView itemView, String model, int position) {
                Glide.with(UnlockTerminalActivity.this)
                        .load(model)
                        .centerCrop()
                        .dontAnimate()
                        .into(itemView);
            }
        });

        banner.setData(new ArrayList<>(AdConstants.AD_URL.keySet()), null);
    }

    @Override
    protected void setListeners(){

    }

    @Override
    protected void loadParam(){

    }

    protected void handleMsg(Message msg){

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(UnlockTerminalActivity.this, OperLogonActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.pos_lockup));
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            intent.putExtras(bundle);
            startActivityForResult(intent, 1);
            return true;
        }
        return false;
    }

    private class LearnGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(velocityY < 0) {// AET-67
                Intent intent = new Intent(UnlockTerminalActivity.this, OperLogonActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.pos_lockup));
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
            }
            return true;
        }
    }

    public static Intent getLockerIntent(){
        return lockerIntent;
    }

    public static Intent setLockerIntent(Intent intent){
        lockerIntent = intent;
        return lockerIntent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if((requestCode == 1) && (resultCode == RESULT_OK)){
            if(lockerIntent != null)
                stopService(lockerIntent);
            finish();
        }
    }
}
