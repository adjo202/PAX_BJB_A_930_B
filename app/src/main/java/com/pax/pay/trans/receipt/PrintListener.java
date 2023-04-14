/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:24
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans.receipt;

public interface PrintListener {
    public static final int CONTINUE = 0;
    public static final int CANCEL = 1;

    /**
     * 打印提示信息
     * 
     * @param title
     * @param message
     */
    public void onShowMessage(String title, String message);

    /**
     * 打印机异常确认
     * 
     * @param title
     * @param message
     * @return {@link PrintListener#CONTINUE}/{@link PrintListener#CANCEL}
     */
    public int onConfirm(String title, String message);

    public void onEnd();
}
