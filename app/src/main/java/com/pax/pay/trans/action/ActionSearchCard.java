package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.activity.SearchCardActivity;
import com.pax.pay.trans.model.TransData;

public class ActionSearchCard extends AAction {

    private Context context;

    private TransData mTransData;

    private byte mode;
    private String title;
    private String amount;
    private String date;
    private String code;
    private String searchCardPrompt;

    private ESearchCardUIType searchCardUIType;

    public enum ESearchCardUIType {
        DEFAULT,
        QUICKPASS,
        EC,
    }

    public ActionSearchCard(ActionStartListener listener) {
        super(listener);
    }

    public ActionSearchCard(TransData data, ActionStartListener listener) {
        super(listener);
        mTransData = data;
    }

    /**
     * 寻卡类型定义
     * 
     * @author Steven.W
     * 
     */
    public static class SearchMode {
        /**
         * 刷卡
         */
        public static final byte SWIPE = 0x01;
        /**
         * 插卡
         */
        public static final byte INSERT = 0x02;
        /**
         * 挥卡
         */
        public static final byte TAP = 0x04;
        /**
         * 支持手输
         */
        public static final byte KEYIN = 0x08;
        /**
         * 扫码支付
         */
        public static final byte QR = 0x03;

    }

    /**
     * 设置参数
     * 
     * @param context
     *            ：上下文
     * @param mode
     *            ：读卡模式
     * @param amount
     *            ：交易模式
     */
    public void setParam(Context context, String title, byte mode, String amount, String code, String date,
            ESearchCardUIType searchCardUIType) {
        this.context = context;
        this.title = title;
        this.mode = mode;
        this.amount = amount;
        this.code = code;
        this.date = date;
        this.searchCardUIType = searchCardUIType;
    }

    public void setParam(Context context, String title, byte mode, String amount, String code, String date,
            ESearchCardUIType searchCardUIType, String searchCardPrompt) {
        this.context = context;
        this.title = title;
        this.mode = mode;
        this.amount = amount;
        this.code = code;
        this.date = date;
        this.searchCardUIType = searchCardUIType;
        this.searchCardPrompt = searchCardPrompt;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        if (amount == null && mTransData != null) {
            amount = mTransData.getAmount();
        }
        if (code == null && mTransData != null) {
            code = mTransData.getOrigAuthCode();
        }
        if (date == null && mTransData != null) {
            date = mTransData.getOrigDate();
        }
        Intent intent = new Intent(context, SearchCardActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.NAV_BACK.toString(), true);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        intent.putExtra(EUIParamKeys.CARD_SEARCH_MODE.toString(), mode);
        intent.putExtra(EUIParamKeys.AUTH_CODE.toString(), code);
        intent.putExtra(EUIParamKeys.TRANS_DATE.toString(), date);
        intent.putExtra(EUIParamKeys.SEARCH_CARD_UI_TYPE.toString(), searchCardUIType);
        intent.putExtra(EUIParamKeys.SEARCH_CARD_PROMPT.toString(), searchCardPrompt);
        context.startActivity(intent);
    }

    public void setMode(byte mode) {
        this.mode = mode;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSearchCardPrompt(String searchCardPrompt) {
        this.searchCardPrompt = searchCardPrompt;
    }

    public void setUiType(ESearchCardUIType searchCardUIType) {
        this.searchCardUIType = searchCardUIType;
    }

    public static class CardInformation {
        private byte searchMode;
        private String track1;
        private String track2;
        private String track3;
        private String pan;
        private String expDate;

        public CardInformation(byte mode, String track1, String track2, String track3, String pan) {
            this.searchMode = mode;
            this.track1 = track1;
            this.track2 = track2;
            this.track3 = track3;
            this.pan = pan;
        }

        public CardInformation(byte mode) {
            this.searchMode = mode;
        }

        public CardInformation(byte mode, String pan) {
            this.searchMode = mode;
            this.pan = pan;
        }

        public CardInformation() {

        }

        public byte getSearchMode() {
            return searchMode;
        }

        public void setSearchMode(byte searchMode) {
            this.searchMode = searchMode;
        }

        public String getTrack1() {
            return track1;
        }

        public void setTrack1(String track1) {
            this.track1 = track1;
        }

        public String getTrack2() {
            return track2;
        }

        public void setTrack2(String track2) {
            this.track2 = track2;
        }

        public String getTrack3() {
            return track3;
        }

        public void setTrack3(String track3) {
            this.track3 = track3;
        }

        public String getPan() {
            return pan;
        }

        public void setPan(String pan) {
            this.pan = pan;
        }

        public String getExpDate() {
            return expDate;
        }

        public void setExpDate(String expDate) {
            this.expDate = expDate;
        }
    }


}
