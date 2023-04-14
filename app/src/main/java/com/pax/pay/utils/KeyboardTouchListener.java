package com.pax.pay.utils;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class KeyboardTouchListener implements View.OnTouchListener {
    private SimpleStyleKeyboardUtil keyboardUtil;
    private int keyboardType = SimpleStyleKeyboardUtil.TYPE_DEFAULT;

    public KeyboardTouchListener(SimpleStyleKeyboardUtil util, int keyBoardType) {
        this.keyboardUtil = util;
        this.keyboardType = keyBoardType;
    }

    /**
     * 默认使用第一种简洁小键盘
     * 
     * @param util
     */
    public KeyboardTouchListener(SimpleStyleKeyboardUtil util) {
        this.keyboardUtil = util;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (keyboardUtil != null && keyboardUtil.getEd() != null && v.getId() != keyboardUtil.getEd().getId())
                keyboardUtil.showKeyBoardLayout((EditText) v, keyboardType);
            else if (keyboardUtil != null && keyboardUtil.getEd() == null) {
                keyboardUtil.showKeyBoardLayout((EditText) v, keyboardType);
            } else {
                if (keyboardUtil != null) {
                    keyboardUtil.setKeyBoardCursorNew((EditText) v);
                }
            }
        }
        return false;
    }
}
