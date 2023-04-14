/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-6
 * Module Author: laiyi
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.emv.clss;


import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;

import java.io.Serializable;


public class ClssTornLog extends AEntityBase implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ID_FIELD_NAME = "id";

    @Column
    private String aucPan;
    @Column(canBeNull = false)
    private int panLen;
    @Column(canBeNull = false)
    private boolean panSeqFlg;
    @Column(canBeNull = false)
    private byte panSeq;
    @Column(canBeNull = false)
    private String aucTornData;
    @Column(canBeNull = false)
    private int tornDataLen;

    public ClssTornLog() {
        //do nothing
    }

    public String getAucPan() {
        return aucPan;
    }

    public void setAucPan(String aucPan) {
        this.aucPan = aucPan;
    }

    public int getPanLen() {
        return panLen;
    }

    public void setPanLen(int panLen) {
        this.panLen = panLen;
    }

    public boolean getPanSeqFlg() {
        return panSeqFlg;
    }

    public void setPanSeqFlg(boolean panSeqFlg) {
        this.panSeqFlg = panSeqFlg;
    }

    public byte getPanSeq() {
        return panSeq;
    }

    public void setPanSeq(byte panSeq) {
        this.panSeq = panSeq;
    }

    public String getAucTornData() {
        return aucTornData;
    }

    public void setAucTornData(String aucTornData) {
        this.aucTornData = aucTornData;
    }

    public int getTornDataLen() {
        return tornDataLen;
    }

    public void setTornDataLen(int tornDataLen) {
        this.tornDataLen = tornDataLen;
    }
}
