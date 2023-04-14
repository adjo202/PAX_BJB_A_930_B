/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-4-25
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.emv.clss;

import com.pax.eemv.entity.TagsTable;

import java.util.ArrayList;
import java.util.List;

class ClssDE55Tag {
    private int emvTag;
    private byte option;
    private int len;

    public static final byte DE55_MUST_SET = 0x10;// 必须存在
    public static final byte DE55_OPT_SET = 0x20;// 可选择存在
    public static final byte DE55_COND_SET = 0x30;// 根据条件存在

    public ClssDE55Tag(int emvTag, byte option, int len) {
        this.emvTag = emvTag;
        this.option = option;
        this.len = len;
    }

    public int getEmvTag() {
        return emvTag;
    }

    public void setEmvTag(int emvTag) {
        this.emvTag = emvTag;
    }

    public byte getOption() {
        return option;
    }

    public void setOption(byte option) {
        this.option = option;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    // 非接消费55域标签
    public static List<ClssDE55Tag> genClssDE55Tags() {
        List<ClssDE55Tag> clssDE55Tags = new ArrayList<>();
        clssDE55Tags.add(new ClssDE55Tag(0x57, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x5A, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x5F24, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x5F2A, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.PAN_SEQ_NO, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x82, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x84, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.TVR, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.TRANS_DATE, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.TSI, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.TRANS_TYPE, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.AMOUNT, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.AMOUNT_OTHER, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x9F08, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x9F09, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x9F10, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.COUNTRY_CODE, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x9F1E, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.APP_CRYPTO, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.CRYPTO, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x9F33, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x9F34, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x9F35, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(TagsTable.ATC, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x9F37, DE55_MUST_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x9F41, DE55_OPT_SET, 0));
        clssDE55Tags.add(new ClssDE55Tag(0x9F5B, DE55_OPT_SET, 0));
        return clssDE55Tags;
    }
}
