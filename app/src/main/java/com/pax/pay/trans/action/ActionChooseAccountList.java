/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-8-9 3:4
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.activity.SelectAccountActivity;
import com.pax.pay.trans.model.AccountData;
import com.pax.pay.trans.model.TransData;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

/**
 * Created by liliang on 2017/8/9.
 */

public class ActionChooseAccountList extends AAction {

    public static final String TAG = "ActionAccountList";
    private Context context;
    private TransData mTransData;
    private String titel;
    private String subtitel;

    public ActionChooseAccountList(Context context, TransData data) {
        this(data, null);
        this.context = context;
    }

    public ActionChooseAccountList(Context context, TransData data, String title, String subtitle) {
        this(data, null);
        this.context = context;
        this.titel = title;
        this.subtitel = subtitle;
    }

    public ActionChooseAccountList(TransData data, ActionStartListener listener) {
        super(listener);
        mTransData = data;

    }

    @Override
    protected void process() {
        String title = "Account List";
        String subTitle = "Pilih Rekening";
        ArrayList<AccountData> nameList = new ArrayList<AccountData>();

        // add
        try {
            String bit120 = mTransData.getField120();

            if (StringUtils.isEmpty(bit120)){
                setResult(new ActionResult(TransResult.ERR_ABORTED, null));
            }else {
                //String[] accType = new String[5], accNo = new String[5];
                String acc = bit120.substring(3);
                String type = "", no = "";
                int totalCut = 22;
                int lenn = acc.length() / totalCut;
                for (int i = 0; i < lenn; i++) {
                    int start = i * totalCut;
                    int end = start + totalCut;
                    String accountType = acc.substring(start,start+2);
                    String accountNumber = acc.substring(start+2,end);
                    //String finalAccount = String.format("%s %s", accountType, accountNumber);
                    nameList.add(new AccountData(accountType,accountNumber));
                }

                mTransData.setAccType(type);
                mTransData.setAccNo(no);
            }

        } catch (Exception e) {
            e.printStackTrace();
            setResult(new ActionResult(TransResult.ERR_ABORTED, null));
        }

        if (StringUtils.isEmpty(titel) || StringUtils.isEmpty(subtitel)) {

            context = TransContext.getInstance().getCurrentContext();
            Intent intent = new Intent(context, SelectAccountActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            bundle.putString(EUIParamKeys.PROMPT_1.toString(), subTitle);
            bundle.putSerializable(EUIParamKeys.CONTENT.toString(), nameList);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }else {
            context = TransContext.getInstance().getCurrentContext();
            Intent intent = new Intent(context, SelectAccountActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), titel);
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            bundle.putString(EUIParamKeys.PROMPT_1.toString(), subtitel);
            bundle.putSerializable(EUIParamKeys.CONTENT.toString(), nameList);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }
}
