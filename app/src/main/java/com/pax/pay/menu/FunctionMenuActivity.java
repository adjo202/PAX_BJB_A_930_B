/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-4-14
 * Module Author: shity
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.menu;

import android.view.View;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.trans.action.function.ActionOnlineParamProcess;
import com.pax.pay.trans.action.function.ActionQueryAppList;
import com.pax.pay.trans.model.ETransType;
import com.pax.up.bjb.R;
import com.pax.view.ListMenu;


public class FunctionMenuActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {

        ListMenu.Builder builder = new ListMenu.Builder(FunctionMenuActivity.this)
                //Sandy : echo test
                .addActionItem(getString(R.string.om_download_menu_echo_test), R.drawable.echo, createOnlineFunction(ETransType.ECHO.toString()))
                //Sandy : Parameter download
                .addActionItem(getString(R.string.om_download_menu_parameter_download), R.drawable.parameter_download, createOnlineFunction(ETransType.DOWNLOAD_PARAM.toString()))
                //.addActionItem(getString(R.string.om_download_menu_download_product_and_data), R.drawable.ppob_download_product,createOnlineFunction(ETransType.EMV_MON_CA.toString()))

                //Sandy : IC card parameter download
                .addActionItem(getString(R.string.om_download_menu_iccard_parameter), R.drawable.ic_parameter_download, createOnlineFunction(ETransType.EMV_MON_PARAM.toString()))
                .addActionItem(getString(R.string.om_download_menu_iccard_public_key), R.drawable.ic_puk_download, createOnlineFunction(ETransType.EMV_MON_CA.toString()))
                .addActionItem(getString(R.string.om_download_menu_iccard_parameter), R.drawable.ic_parameter_download, createOnlineFunction(ETransType.EMV_MON_PARAM.toString()))
                //app list
                .addActionItem(getString(R.string.om_query_menu_applist), R.drawable.query_app_list, createAppListAction());

        return builder.create();
    }

    private AAction createAppListAction() {

        AAction action = new ActionQueryAppList(this, null);
        action.setEndListener(new AAction.ActionEndListener() {
            @Override
            public void onEnd(AAction action, ActionResult result) {
                ActivityStack.getInstance().popAllButBottom();
            }
        });
        return action;
    }

    private AAction createOnlineFunction(final String transTpye) {
        ActionOnlineParamProcess actionOnlineParamProcess = new ActionOnlineParamProcess(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionOnlineParamProcess) action).setParam(FunctionMenuActivity.this, transTpye);
            }
        });

        actionOnlineParamProcess.setEndListener(new AAction.ActionEndListener() {
            @Override
            public void onEnd(AAction action, ActionResult result) {
                ActivityStack.getInstance().popAllButBottom();
            }
        });

        return actionOnlineParamProcess;
    }

}




