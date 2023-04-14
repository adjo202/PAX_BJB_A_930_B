package com.pax.pay.trans.model;

import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;

import java.io.Serializable;

/**
 * 交易总计
 * 
 * @author Steven.W
 * 
 */
public class ProductData extends AEntityBase implements Serializable {

    public static final String TAG = "ProductData";

    private static final long serialVersionUID = 1L;


    @Column
    private String productId;

    // add abdul
    @Column
    private String productType;

    @Column
    private String productName;

    @Column
    private String productDescription;

    @Column
    private String operator;

    @Column
    private String basePrice;

    @Column
    private String sellPrice;

    @Column
    private String fee;


    public ProductData() {

    }

    public ProductData(String productId, String basePrice, String sellPrice, String fee) {
        this.productId = productId;
        this.basePrice = basePrice;
        this.sellPrice = sellPrice;
        this.fee = fee;
    }

    public ProductData(String productId, String productType, String productName, String productDescription, String operator , String basePrice, String sellPrice, String fee) {
        this.productId = productId;
        this.productType = productType;
        this.productName = productName;
        this.productDescription = productDescription;
        this.operator = operator;
        this.basePrice = basePrice;
        this.sellPrice = sellPrice;
        this.fee = fee;
    }

    /*public ProductData(String productId, String productType, String productName,String productDescription, String operator, String basePrice, String sellPrice, String fee) {
        this.productId = productId;
        this.productType = productType;
        this.productName = productName;
        this.productDescription = productDescription;
        this.operator = operator;
        this.basePrice = basePrice;
        this.sellPrice = sellPrice;
        this.fee = fee;
    }*/



    public static String getTAG() {
        return TAG;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    public String getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(String sellPrice) {
        this.sellPrice = sellPrice;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }


    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    @Override
    public String toString() {
        return "ProductData{" +
                "productId='" + productId + '\'' +
                ", productType='" + productType + '\'' +
                ", productName='" + productName + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", operator='" + operator + '\'' +
                ", basePrice='" + basePrice + '\'' +
                ", sellPrice='" + sellPrice + '\'' +
                ", fee='" + fee + '\'' +
                '}';
    }
}
