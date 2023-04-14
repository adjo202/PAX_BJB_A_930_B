/*
 * *
 *     * ********************************************************************************
 *     * COPYRIGHT
 *     *               PAX TECHNOLOGY, Inc. PROPRIETARY INFORMATION
 *     *   This software is supplied under the terms of a license agreement or
 *     *   nondisclosure agreement with PAX  Technology, Inc. and may not be copied
 *     *   or disclosed except in accordance with the terms in that agreement.
 *     *
 *     *      Copyright (C) 2017 PAX Technology, Inc. All rights reserved.
 *     * ********************************************************************************
 *
 */

package com.pax.pay.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.device.Device;
import com.pax.market.api.sdk.java.api.param.ParamApi;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.service.dto.PageInfoDto;
import com.pax.pay.service.dto.ParamDto;
import com.pax.pay.service.dto.RequestDto;
import com.pax.settings.SysParam;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.DialogUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by zcy on 2016/12/2 0002.
 */
public class DownloadParamService extends Service {
    private static final String TAG = "DownloadParamService";
    private List<RequestDto> mDto = new ArrayList<>();

    //以下需要替换 , 一定要修改MANIFEST里面的PACKAGENAME，
    // packageName和versioncode与后台配的参数一致，是收到应用市场广播的前提。

    private static final String BASE_URL = "https://api.whatspos.cn/p-market-api/v1";
    private static final String APP_KEY = "EHYDLMUZS0X6EAUHKGO5";
    private static final String APP_SECRET = "P2FJFHIXES1E7MF7MKIP7SIZGJ7WLYGVDAES5XA6";
    private static final String PACKAGENAME = FinancialApplication.getAppContext().getPackageName(); //
    // 注意这里必须与自己应用的PACKAGENAME相同
    private static final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath() +
            File.separator + "/ParamDownload/";
    private DownloadTask downloadTask;
    private CustomAlertDialog dialog;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            //获取参数列表成功
            if (msg.obj != null && !((PageInfoDto) msg.obj).getList().isEmpty()) {
                List<ParamDto> listDto = ((PageInfoDto) msg.obj).getList();
                if (listDto != null && !listDto.isEmpty()) {
                    ParamDto paramDto = listDto.get(0);
                    if (downloadTask != null) {
                        downloadTask.cancel(true);
                    }
                    downloadTask = new DownloadTask();
                    downloadTask.execute(paramDto.getDownloadUrl(), SAVE_PATH, paramDto
                            .getActionId(), listDto);
                }
            } else {
                showResult(false);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DialogUtils.showUpdateDialog(ActivityStack.getInstance().top(), "Received a Param push " +
                        "message! Update?",
                new CustomAlertDialog.OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        getParamList();
                        dialog = new CustomAlertDialog(ActivityStack.getInstance().top(),
                                CustomAlertDialog.PROGRESS_TYPE);
                        dialog.setTitleText("Downloading");
                        dialog.show();
                        dialog.setCancelable(false);
                    }
                });
        return super.onStartCommand(intent, flags, startId);
    }

    private void getParamList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<ETermInfoKey, String> map = FinancialApplication.getDal().getSys().getTermInfo();
                String terminalSn = map.get(ETermInfoKey.SN);
                ParamApi.init(BASE_URL, APP_KEY, APP_SECRET, terminalSn);
                try {
                    PackageManager pm = FinancialApplication.getAppContext().getPackageManager();
                    PackageInfo pi = pm.getPackageInfo(PACKAGENAME, PackageManager
                            .GET_CONFIGURATIONS);
                    PageInfoDto pageInfoDto = getDownloadUrl(PACKAGENAME,  pi.versionCode);
                    Message message = new Message();
                    message.obj = pageInfoDto;
                    mHandler.sendMessage(message);
                } catch (PackageManager.NameNotFoundException e) {
                    showResult(false);
                    Log.e(TAG, "", e);
                }
            }
        }).start();

    }

    /**
     * 获取参数下载url
     *
     * @param packageName
     * @param versionCode
     * @return
     */
    private PageInfoDto getDownloadUrl(String packageName, int versionCode) {
        String result = ParamApi.getParamDownloadList(packageName, versionCode);
        if (!TextUtils.isEmpty(result)) {
            return transferToDto(result);
        }
        return null;
    }

    /**
     * 只下载第一条param，其他的都返回为错误。
     *
     * @param listDto
     */
    private void updateOtherActions(List<ParamDto> listDto) {
        if (listDto.size() > 1) {
            mDto.remove(0);
            for (RequestDto dto : mDto) {
                dto.setStatus(ParamApi.ACT_STATUS_FAILED);
                dto.setErrorCode(ParamApi.CODE_PARAM_DUPLICATE);
            }
            Gson gson = new Gson();
            String requestBody = gson.toJson(mDto);
            updateActionStatus(requestBody);
        }
    }

    /**
     * 批量更新操作状态
     *
     * @param requestBody
     */
    public void updateActionStatus(final String requestBody) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParamApi.updateDownloadStatusBatch(requestBody);
                mDto.clear();
            }
        }).start();
    }

    /**
     * 结果转化为本地DTO
     *
     * @param result
     * @return
     */
    @Nullable
    private PageInfoDto transferToDto(String result) {
        Type typeList = new TypeToken<PageInfoDto>() {
        }.getType();
        Gson gson = new Gson();
        PageInfoDto pageInfoDto = gson.fromJson(result, typeList);
        List<ParamDto> list = pageInfoDto.getList();
        if (list != null && !list.isEmpty()) {
            for (ParamDto dto : list) {
                RequestDto requestDto = new RequestDto();
                requestDto.setActionId(dto.getActionId());
                requestDto.setStatus(ParamApi.ACT_STATUS_PENDING);
                mDto.add(requestDto);
            }
            return pageInfoDto;
        }
        return null;
    }


    /**
     * 显示参数下载结果
     *
     * @param result
     * @return
     */
    private void showResult(boolean result) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        CustomAlertDialog showResultDialog = null;
        if (result) {
            showResultDialog = new CustomAlertDialog(ActivityStack.getInstance().top(),
                    CustomAlertDialog.SUCCESS_TYPE, 3);
            showResultDialog.setTitleText("Download Param Succ!");
            showResultDialog.show();
            showResultDialog.setCancelable(false);
            Device.beepOk();
        } else {
            showResultDialog = new CustomAlertDialog(ActivityStack.getInstance().top(),
                    CustomAlertDialog.ERROR_TYPE, 3);
            showResultDialog.setTitleText("Download Param Failed!");
            showResultDialog.show();
            showResultDialog.setCancelable(false);
            Device.beepErr();
        }
    }

    /**
     * 下载参数
     */
    class DownloadTask extends AsyncTask<Object, Void, Void> {
        List<ParamDto> listDto = null;

        @Override
        protected Void doInBackground(Object... params) {
            listDto = (List<ParamDto>) params[3];
            ParamApi.downloadParamFile((String) params[0], (String) params[1], String.valueOf
                    (params[2]));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            boolean result = SysParam.init(ActivityStack.getInstance().top());
            if (result) {
                updateOtherActions(listDto);
            }
            showResult(result);
        }
    }
}

