package com.pax.pay.menu;

import android.content.Intent;
import android.os.Bundle;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionEndListener;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.UnlockTerminalActivity;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.service.LockService;
import com.pax.pay.trans.PosLogout;
import com.pax.pay.trans.SettleTrans;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionConfirmLock;
import com.pax.pay.trans.action.ActionDispMultiLineMsg;
import com.pax.pay.trans.action.ActionDispSingleLineMsg;
import com.pax.pay.trans.action.ActionInputPasword;
import com.pax.settings.SettingsActivity;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;
import com.pax.view.dialog.DialogUtils;

public class ManageMenuActivity extends BaseMenuActivity {

    @Override
    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(ManageMenuActivity.this, 9, 3)
                // 签到
                .addMenuItem(getString(R.string.pos_logon), R.drawable.app_poslogon, LogonMenuActivity.class)

                // 签退
                .addTransItem(getString(R.string.trans_logout), R.drawable.app_logout,
                        new PosLogout(ManageMenuActivity.this, handler, new ATransaction.TransEndListener() {

                            @Override
                            public void onEnd(ActionResult result) {
                                if (result.getRet() != TransResult.SUCC) {
                                    return;
                                }
                                // handler.sendEmptyMessage(CHECK_OPER_LOGIN);
                            }
                        }))

                // 交易查询 move to the main menu by richard 20170420
                //.addMenuItem(getString(R.string.trans_query), R.drawable.app_query, TransQueryActivity.class)




                // 结算
                .addTransItem(getString(R.string.trans_settle), R.drawable.app_settle,
                        new SettleTrans(ManageMenuActivity.this, handler, null))

                //LOCKUP TERMINAL
                .addActionItem(getString(R.string.pos_lockup), R.drawable.modify_mag_passwd,
                        createConfirmLockAction())

                // 操作员管理
                .addActionItem(getString(R.string.oper_manage), R.drawable.app_opermag,
                        createInputPwdActionForManageOper())
                // 终端设置
                .addActionItem(getString(R.string.settings_title), R.drawable.app_setting,
                        createInputPwdActionForSettings())

                // 查看版本
                .addActionItem(getString(R.string.about), R.drawable.app_version,
                        createMultipleDispActionForVersion());
        return builder.create();
    }






    private AAction createMultipleDispActionForVersion() {

        ActionDispMultiLineMsg displayInfoAction = new ActionDispMultiLineMsg(new ActionStartListener() {
            @Override
            public void onStart(AAction action) {


                String[] display = new String[]{
                        String.format("TID         : %s", FinancialApplication.getSysParam().get(SysParam.TERMINAL_ID)),
                        String.format("MID        : %s", FinancialApplication.getSysParam().get(SysParam.MERCH_ID)),
                        String.format("Version  : %s", FinancialApplication.version)
                };

                ((ActionDispMultiLineMsg) action).setParam(ManageMenuActivity.this, handler,
                        getString(R.string.version),
                        getString(R.string.app_version),
                        display, 60);
            }
        });

        displayInfoAction.setEndListener(new ActionEndListener() {
            @Override
            public void onEnd(AAction action, ActionResult result) {
                ActivityStack.getInstance().popTo(ManageMenuActivity.this);
            }
        });

        return displayInfoAction;
    }



    private AAction createDispActionForVersion() {
        ActionDispSingleLineMsg displayInfoAction = new ActionDispSingleLineMsg(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionDispSingleLineMsg) action).setParam(ManageMenuActivity.this, handler,
                        getString(R.string.version), getString(R.string.app_version), FinancialApplication.version, 60);
            }
        });

        displayInfoAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                ActivityStack.getInstance().popAllButBottom();
            }
        });

        return displayInfoAction;
    }

    private AAction createInputPwdActionForSettings() {
        ActionInputPasword inputPaswordAction = new ActionInputPasword(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPasword) action).setParam(ManageMenuActivity.this, handler, 8,
                        getString(R.string.prompt_sys_pwd), null);
            }
        });

        inputPaswordAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {

                if (result.getRet() != TransResult.SUCC) {
                    return;
                }

                String data = (String) result.getData();
                if (!FinancialApplication.getSysParam().get(SysParam.SEC_SYSPWD).equals(data)) {
                    DialogUtils.showErrMessage(ManageMenuActivity.this, handler, getString(R.string.settings_title),
                            getString(R.string.err_password), null, Constants.FAILED_DIALOG_SHOW_TIME);
                    return;
                }
                Intent intent = new Intent(ManageMenuActivity.this, SettingsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.settings_title));
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

        return inputPaswordAction;

    }

    private AAction createInputPwdActionForManageOper() {
        ActionInputPasword inputPaswordAction = new ActionInputPasword(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPasword) action).setParam(ManageMenuActivity.this, handler, 6,
                        getString(R.string.prompt_director_pwd), null);
            }
        });

        inputPaswordAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                if (result.getRet() != TransResult.SUCC) {
                    return;
                }

                String data = (String) result.getData();
                if (!FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD).equals(data)) {
                    DialogUtils.showErrMessage(ManageMenuActivity.this, handler, getString(R.string.oper_manage),
                            getString(R.string.err_manager_password), null, Constants.FAILED_DIALOG_SHOW_TIME);
                    return;
                }
                Intent intent = new Intent(ManageMenuActivity.this, OperMenuActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.oper_manage));
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        return inputPaswordAction;
    }

    private AAction createConfirmLockAction() {
        ActionConfirmLock confirmLock = new ActionConfirmLock(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionConfirmLock) action).setParam(ManageMenuActivity.this, handler);
            }
        });

        confirmLock.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {

                if (result.getRet() != TransResult.SUCC) {
                    return;
                }

                if(UnlockTerminalActivity.getLockerIntent() == null){
                    startService(UnlockTerminalActivity.setLockerIntent(new Intent(ManageMenuActivity.this, LockService.class)));
                } else {
                    startService(UnlockTerminalActivity.getLockerIntent());
                }

                Intent intent = new Intent(ManageMenuActivity.this, UnlockTerminalActivity.class);
                startActivity(intent);

            }
        });

        return confirmLock;

    }
}
