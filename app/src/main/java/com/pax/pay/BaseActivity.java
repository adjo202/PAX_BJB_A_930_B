package com.pax.pay;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.app.quickclick.MenuQuickClickProtection;
import com.pax.pay.app.quickclick.QuickClickProtection;
import com.pax.pay.trans.TransContext;
import com.pax.pay.utils.Fox;
import com.pax.settings.currency.Currency;

import java.util.LinkedHashMap;

@SuppressLint("HandlerLeak")
public abstract class BaseActivity extends FragmentActivity {

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            handleMsg(msg);
        }
    };

    protected QuickClickProtection quickClickProtection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quickClickProtection = new QuickClickProtection();
        //Initialization to prevent the menu item from being unresponsive after the page jumps
        //getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE); // remark
        MenuQuickClickProtection.getInstance().init();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayoutId());

        loadParam();
        initViews();
        setListeners();
        ActivityStack.getInstance().push(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TransContext.getInstance().setCurrentContext(this);
    }

    @Override
    protected void onDestroy() {
        ActivityStack.getInstance().removeTop(this);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * 获取布局文件ID
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 初始化控件
     */
    protected abstract void initViews();

    /**
     * 设置监听器
     */
    protected abstract void setListeners();

    /**
     * 加载调用参数
     */
    protected abstract void loadParam();

    /**
     * handler消息处理
     *
     * @param msg
     */
    protected abstract void handleMsg(Message msg);

    /**
     * app不受系统字体大小影响
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics() );
        return res;
    }
}
