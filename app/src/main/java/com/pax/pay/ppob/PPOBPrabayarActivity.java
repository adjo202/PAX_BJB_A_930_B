package com.pax.pay.ppob;

import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivity;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.pulse.ListDataFragment;
import com.pax.pay.pulse.ListOthersPrabayarFragment;
import com.pax.pay.pulse.ListPLNFragment;
import com.pax.pay.pulse.ListPulsaFragment;
import com.pax.pay.trans.PosDownloadProduct;
import com.pax.up.bjb.R;
import com.pax.view.PagerSlidingTabStrip;
import com.pax.view.dialog.MenuPopupWindow.ActionItem;
import com.pax.view.dialog.MenuPopupWindow.OnItemOnClickListener;

public class PPOBPrabayarActivity extends BaseActivity implements OnClickListener,OnItemOnClickListener {

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private ImageView backBtn;
    private TextView headerText;
    private ImageView searchBtn;
    private ImageView printBtn;

    private String[] titles;

    private ListPulsaFragment listPulsaFragment;
    private ListDataFragment listDataFragment;
    private ListPLNFragment listPLNFragment;
    private ListOthersPrabayarFragment listOthersPrabayarFragment;

    private String navTitle;
    private boolean supportDoTrans;
    private static final String TAG = "[PPOBPrabayarActivity]";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_trans_pulse_layout;
    }

    public ATransaction.TransEndListener listener = new ATransaction.TransEndListener() {
        @Override
        public void onEnd(ActionResult result) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    /*hasDoTrans = false;// 重置交易标志位
                    resetUI();*/
                }
            });

        }
    };


    @Override
    protected void loadParam() {
        titles = new String[] { getString(R.string.trans_pln),getString(R.string.trans_pulsa), getString(R.string.trans_data),  getString(R.string.trans_other) };
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        supportDoTrans = getIntent().getBooleanExtra(EUIParamKeys.SUPPORT_DO_TRANS.toString(), true);
    }


    private void initMenuPopupWindow() {
        /*popupWindow = new MenuPopupWindow(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        popupWindow.addAction(new ActionItem(this, getString(R.string.trans_reload), R.drawable.i2));*/
    }

    @Override
    public void onItemClick(ActionItem item, int position) {
        CharSequence title = item.getTitle();
        if (title.equals(getString(R.string.trans_reload))) {
            new PosDownloadProduct( PPOBPrabayarActivity.this, handler, listener).execute();
        }

    }

    public class MyAdapter extends FragmentPagerAdapter {
        String[] _titles;

        public MyAdapter(FragmentManager fm, String[] titles) {
            super(fm);
            _titles = titles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return _titles[position];
        }

        @Override
        public int getCount() {
            return _titles.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Log.d("teg",TAG+"listPLNFragment");
                    try {
                        if(listPLNFragment == null){
                            listPLNFragment = new ListPLNFragment();
                        }
                        return listPLNFragment;
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                case 1:
                    Log.d("teg",TAG+"listPulsaFragment");
                    try {
                        if (listPulsaFragment == null) {
                            listPulsaFragment = new ListPulsaFragment();
                        }
                        return listPulsaFragment;

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                case 2:
                    Log.d("teg",TAG+"listDataFragment");
                    try {
                        if (listDataFragment == null) {
                            listDataFragment = new ListDataFragment();
                        }
                        return listDataFragment;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                case 3:
                    Log.d("teg",TAG+"listOthersPrabayarFragment");
                    try {
                        if (listOthersPrabayarFragment == null) {
                            listOthersPrabayarFragment = new ListOthersPrabayarFragment();
                        }
                        return listOthersPrabayarFragment;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                default:
                    Log.d("teg",TAG+"default");
                    return null;
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.header_back:
                setResult(100);
                finish();
                break;
            /*case R.id.print_btn:
                popupWindow.show(v);
                break;*/
            /*case R.id.search_btn:
                queryTransRecordByTransNo();
                break;*/
            default:
                break;
        }

    }


    @Override
    protected void initViews() {
        headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(navTitle);
        backBtn = (ImageView) findViewById(R.id.header_back);

        searchBtn = (ImageView) findViewById(R.id.search_btn);
        //searchBtn.setVisibility(View.VISIBLE);
        //sandy : because its error, temporary disabled
        searchBtn.setVisibility(View.GONE);

        printBtn = (ImageView) findViewById(R.id.print_btn);
        //printBtn.setVisibility(View.VISIBLE);

        pager = (ViewPager) findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager.setAdapter(new MyAdapter(getSupportFragmentManager(), titles));
        tabs.setViewPager(pager);

        //initMenuPopupWindow();

    }

    @Override
    protected void setListeners() {
        backBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        printBtn.setOnClickListener(this);
        //popupWindow.setItemOnClickListener(this);

    }

    @Override
    protected void handleMsg(Message msg) {

    }

}
