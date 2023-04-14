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

package com.pax.pay.service.dto;

import java.util.List;

public class PageInfoDto {
    private int totalCount;
    private int businessCode;
    private List<ParamDto> list;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(int businessCode) {
        this.businessCode = businessCode;
    }

    public List<ParamDto> getList() {
        return list;
    }

    public void setList(List<ParamDto> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "PageInfoDto{" +
                "totalCount=" + totalCount +
                ", businessCode=" + businessCode +
                ", list=" + list +
                '}';
    }
}