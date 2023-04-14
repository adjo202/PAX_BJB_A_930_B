package com.pax.pay.pulse;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivity;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.PosDownloadProduct;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.model.TransData;
import com.pax.up.bjb.R;
import com.pax.view.PagerSlidingTabStrip;
import com.pax.view.dialog.DialogUtils;
import com.pax.view.dialog.MenuPopupWindow;
import com.pax.view.dialog.MenuPopupWindow.ActionItem;
import com.pax.view.dialog.MenuPopupWindow.OnItemOnClickListener;

public class PulseActivity extends BaseActivity implements OnClickListener,OnItemOnClickListener {

    private PagerSlidingTabStrip tabs;

    private ViewPager pager;

    private ImageView backBtn;
    private TextView headerText;

    private MenuPopupWindow popupWindow;

    private ImageView searchBtn;
    private ImageView printBtn;

    private String[] titles;

    private ListPulsaFragment listPulsaFragment;
    private ListDataFragment listDataFragment;

    private String navTitle;
    private boolean supportDoTrans;
    private static final String TAG = "PulseActivity";

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
        titles = new String[] { getString(R.string.trans_pulsa), getString(R.string.trans_data) };
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        supportDoTrans = getIntent().getBooleanExtra(EUIParamKeys.SUPPORT_DO_TRANS.toString(), true);
    }


    private void initMenuPopupWindow() {
        popupWindow = new MenuPopupWindow(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        popupWindow.addAction(new ActionItem(this, getString(R.string.trans_reload), R.drawable.i2));
        /*
        popupWindow.addAction(new ActionItem(this, getString(R.string.print_trans_total), R.drawable.i3));
        popupWindow.addAction(new ActionItem(this, getString(R.string.print_last_total), R.drawable.i4));
        */
    }

    @Override
    public void onItemClick(ActionItem item, int position) {
        CharSequence title = item.getTitle();
        if (title.equals(getString(R.string.trans_reload))) {
            new PosDownloadProduct( PulseActivity.this, handler, listener).execute();
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
                    if (listPulsaFragment == null) {
                        listPulsaFragment = new ListPulsaFragment();
                        /*
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(EUIParamKeys.SUPPORT_DO_TRANS.toString(), supportDoTrans);
                        listPulsaFragment.setArguments(bundle);
                         */
                    }
                    return listPulsaFragment;
                case 1:
                    if (listDataFragment == null) {
                        listDataFragment = new ListDataFragment();
                    }
                    return listDataFragment;
                default:
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
            case R.id.print_btn:
                popupWindow.show(v);
                break;
            case R.id.search_btn:
                queryTransRecordByTransNo();
                break;
            default:
                break;
        }

    }

    private void queryTransRecordByTransNo() {
        ActionInputTransData inputTransDataAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(PulseActivity.this, handler,
                        getString(R.string.trans_query)).setInfoTypeSale(getString(R.string.prompt_input_transno),
                        EInputType.NUM, 6, 1, false);

                TransContext.getInstance().setCurrentAction(action);
            }

        }, 1);


        inputTransDataAction.setEndListener(new AAction.ActionEndListener() {


            @Override
            public void onEnd(AAction action, ActionResult result) {
                Log.d(TAG,"Sandy.TransQueryActivity.onEnd " + result.getRet());

                if (result.getRet() != TransResult.SUCC) {
                    ActivityStack.getInstance().pop();
                    return;
                }

                String content = (String) result.getData();
                long transNo = Long.parseLong(content);
                final TransData transData = TransData.readTrans(transNo);


                if (transData == null) {
                    DialogUtils.showErrMessage(TransContext.getInstance().getCurrentContext(), handler,
                            getString(R.string.trans_query), getString(R.string.orig_trans_no_exist),
                            new OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface arg0) {
                                    ActivityStack.getInstance().pop();
                                }
                            }, Constants.FAILED_DIALOG_SHOW_TIME);
                    return;
                }

               // final LinkedHashMap<String, String> map = prepareValuesForDisp(transData);

                ActionDispTransDetail dispTransDetailAction = new ActionDispTransDetail(
                        new AAction.ActionStartListener() {

                            @Override
                            public void onStart(AAction action) {


                             Log.d(TAG,"Sandy.TransQueryActivity.dispTransDetailAction.onStart " + TransContext.getInstance().getCurrentAction());
                            ((ActionDispTransDetail) action).setParam(PulseActivity.this, handler,
                                        getString(R.string.trans_detail), transData, supportDoTrans, 1);

                                TransContext.getInstance().setCurrentAction(action);
                            }
                        });





                dispTransDetailAction.setEndListener(new AAction.ActionEndListener() {

                    @Override
                    public void onEnd(AAction action, ActionResult result) {
                        ActivityStack.getInstance().pop();
                    }
                });
                dispTransDetailAction.execute();
            }
        });

        inputTransDataAction.execute();

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
        printBtn.setVisibility(View.VISIBLE);

        pager = (ViewPager) findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager.setAdapter(new MyAdapter(getSupportFragmentManager(), titles));
        tabs.setViewPager(pager);

        initMenuPopupWindow();

    }

    @Override
    protected void setListeners() {
        backBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        printBtn.setOnClickListener(this);
        popupWindow.setItemOnClickListener(this);

    }

    @Override
    protected void handleMsg(Message msg) {

    }

}
