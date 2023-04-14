/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-6-15
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.eemv;

import com.pax.eemv.entity.AidParam;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.entity.Capk;
import com.pax.eemv.entity.Config;
import com.pax.eemv.entity.EcRecord;
import com.pax.eemv.entity.InputParam;
import com.pax.eemv.exception.EmvException;
import com.pax.jemv.clcommon.ByteArray;

import java.util.ArrayList;
import java.util.List;

public interface IEmvBase {
    void init() throws EmvException;

    void setConfig(Config config);

    Config getConfig();

    byte[] getTlv(int tag);

    void setTlv(int tag, byte[] value) throws EmvException;

    CTransResult process(InputParam inputParam) throws EmvException;

    String processGetPanOnly(InputParam inputParam) throws EmvException;//kd add 01042021

    void setCapkList(List<Capk> capkList);

    void setAidParamList(List<AidParam> aidParamList);

    String getVersion();

    long getEcBalance(long transNo) throws EmvException;

    ArrayList<EcRecord> readAllLoadLogs() throws EmvException;
}
