package com.pax.pay.utils;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.ProductData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tri on 23/07/2021.
 */

public class Controllers {
    public static final String PLN = "plnpra";
    public static final String OTHERS = "OthersPra";
    public static final String PULSA = "PULSA";
    public static final String PDAM = "PDAMPASCA";
    public static final String PASCABAYAR = "Pascabayar";

    public static List<String> getOperatorByType(String tipe) {
        List<String> list = new ArrayList<>();
        List<String> listWithoutDuplicates = new ArrayList<>();
        JSONObject dat;
        String data = FinancialApplication.getSysParam().get("download");

        if (TextUtils.isEmpty(data)) return null;

        try {
            dat = new JSONObject(data);

            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String type = par.getString("type");
                type = type.trim();
                if (type.equalsIgnoreCase(tipe)) {

                    String operator = par.getString("operator").toUpperCase().trim();

                    list.add(operator);
                }
            }

            listWithoutDuplicates = new ArrayList<>(new LinkedHashSet<>(list)); //distinct object

        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return listWithoutDuplicates;
    }


    public static List<ProductData> getAllProductDataByType(String tipe) {
        List<ProductData> list = new ArrayList<>();
        JSONObject dat;
        ProductData productData;
        String data = FinancialApplication.getSysParam().get("download");

        if (TextUtils.isEmpty(data)) return null;

        try {
            dat = new JSONObject(data);

            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String type = par.getString("type");
                type = type.trim();
                if (type.equalsIgnoreCase(tipe)) {

                    String productId = par.getString("productId");
                    String productName = par.getString("productName").trim();
                    String productDesc = par.getString("productDesc").trim();
                    String operator = par.getString("operator");
                    String basePrice = par.getString("basePrice");
                    basePrice = basePrice.substring(0, basePrice.length() - 2);
                    Long base = Long.parseLong(basePrice);
                    basePrice = String.valueOf(base);
                    String sellPrice = par.getString("sellPrice");
                    sellPrice = sellPrice.substring(0, sellPrice.length() - 2);
                    Long sell = Long.parseLong(sellPrice);
                    sellPrice = String.valueOf(sell);
                    String fee = par.getString("fee");
                    fee = fee.substring(0, fee.length() - 2);
                    Long feeL = Long.parseLong(fee);
                    fee = String.valueOf(feeL);
                    operator = operator.toUpperCase().trim();

                    productData = new ProductData(productId, type, productName, productDesc, operator, basePrice, sellPrice, fee);
//                    Log.i("teg", "-->" + productData.toString());
                    list.add(productData);
                }
            }

        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<ProductData> getAllProductDataByType(String tipe, String oprator) {
        List<ProductData> list = new ArrayList<>();
        ProductData productData;
        String data = FinancialApplication.getSysParam().get("download");

        if (TextUtils.isEmpty(data)) return null;

        try {
            JSONObject dat = new JSONObject(data);

            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String type = par.getString("type").trim();
                String operator = par.getString("operator").trim();
                //Log.d("teg", "type : "+type+" param : "+tipe+"| operator : "+operator+" param : "+oprator);

                if (type.equalsIgnoreCase(tipe) && operator.equalsIgnoreCase(oprator)) {
                    Log.d("-->", "type : " + type + " param : " + tipe + "| operator : " + operator + " param : " + oprator);

                    String productId = par.getString("productId");
                    String productName = par.getString("productName").trim();
                    String productDesc = par.getString("productDesc").trim();

                    String basePrice = par.getString("basePrice");
                    basePrice = basePrice.substring(0, basePrice.length() - 2);
                    Long base = Long.parseLong(basePrice);
                    basePrice = String.valueOf(base);
                    String sellPrice = par.getString("sellPrice");
                    sellPrice = sellPrice.substring(0, sellPrice.length() - 2);
                    Long sell = Long.parseLong(sellPrice);
                    sellPrice = String.valueOf(sell);
                    String fee = par.getString("fee");
                    fee = fee.substring(0, fee.length() - 2);
                    Long feeL = Long.parseLong(fee);
                    fee = String.valueOf(feeL);

                    productData = new ProductData(productId, type, productName, productDesc, operator, basePrice, sellPrice, fee);
                    //Log.i("teg", "-->" + productData.toString());
                    list.add(productData);
                }
            }

        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    private static String tambah(String a, String b) {
        if (TextUtils.isEmpty(a)) a = "0";
        if (TextUtils.isEmpty(b)) b = "0";
        return String.valueOf(Long.parseLong(a) + Long.parseLong(b));
    }

    public static int updateProductDataById(String tipe, ProductData prdData) {
        String data = FinancialApplication.getSysParam().get("download");

        if (TextUtils.isEmpty(data)) return -1;

        try {
            JSONObject dat = new JSONObject(data);
            JsonObject body = new JsonObject();

            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String type = par.getString("type").trim();
                String productId = par.getString("productId");
                String operator = par.getString("operator");
                String productName = par.getString("productName");
                String productDesc = par.getString("productDesc");

                if (type.equalsIgnoreCase(tipe) && productId.equalsIgnoreCase(prdData.getProductId())) {
                    //Log.d("-->", "type : "+type+" param : "+tipe+"| operator : "+operator+" param : "+oprator);
                    String basePrice = prdData.getBasePrice();
                    String fee = prdData.getFee();
                    String sellPrice = tambah(basePrice, fee);

                    JsonObject isi = new JsonObject();
                    isi.addProperty("productId", productId);
                    isi.addProperty("productName", productName);
                    isi.addProperty("productDesc", productDesc);
                    isi.addProperty("operator", operator);
                    isi.addProperty("basePrice", basePrice);
                    isi.addProperty("sellPrice", sellPrice);
                    isi.addProperty("fee", fee);
                    isi.addProperty("type", type);

                    body.add("body" + i, isi);


                } else {
                    //Log.d("-->", "type : "+type+" param : "+tipe+"| operator : "+operator+" param : "+oprator);
                    String basePrice = par.getString("basePrice");
                    /*basePrice = basePrice.substring(0, basePrice.length() - 2);
                    Long base = Long.parseLong(basePrice);
                    basePrice = String.valueOf(base);*/
                    String sellPrice = par.getString("sellPrice");
                    /*sellPrice = sellPrice.substring(0, sellPrice.length() - 2);
                    Long sell = Long.parseLong(sellPrice);
                    sellPrice = String.valueOf(sell);*/
                    String fee = par.getString("fee");
                    /*fee = fee.substring(0, fee.length() - 2);
                    Long feeL = Long.parseLong(fee);
                    fee = String.valueOf(feeL);*/

                    JsonObject isi2 = new JsonObject();
                    isi2.addProperty("productId", productId);
                    isi2.addProperty("productName", productName);
                    isi2.addProperty("productDesc", productDesc);
                    isi2.addProperty("operator", operator);
                    isi2.addProperty("basePrice", basePrice);
                    isi2.addProperty("sellPrice", sellPrice);
                    isi2.addProperty("fee", fee);
                    isi2.addProperty("type", type);

                    body.add("body" + i, isi2);

                }
            }

            FinancialApplication.getSysParam().set("download", body.toString());

            return 0;

        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static int updateProductDataById(ProductData prdData) {
        String data = FinancialApplication.getSysParam().get("download");

        if (TextUtils.isEmpty(data)) return -1;

        try {
            JSONObject dat = new JSONObject(data);
            JsonObject body = new JsonObject();

            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String type = par.getString("type").trim();
                String productId = par.getString("productId");
                String operator = par.getString("operator");
                String productName = par.getString("productName");
                String productDesc = par.getString("productDesc");

                if (productId.equalsIgnoreCase(prdData.getProductId())) {
                    //Log.d("-->", "type : "+type+" param : "+tipe+"| operator : "+operator+" param : "+oprator);
                    String basePrice = prdData.getBasePrice();
                    String fee = prdData.getFee();
                    String sellPrice = tambah(basePrice, fee);

                    JsonObject isi = new JsonObject();
                    isi.addProperty("productId", productId);
                    isi.addProperty("productName", productName);
                    isi.addProperty("productDesc", productDesc);
                    isi.addProperty("operator", operator);
                    isi.addProperty("basePrice", basePrice);
                    isi.addProperty("sellPrice", sellPrice);
                    isi.addProperty("fee", fee);
                    isi.addProperty("type", type);

                    body.add("body" + i, isi);


                } else {
                    //Log.d("-->", "type : "+type+" param : "+tipe+"| operator : "+operator+" param : "+oprator);
                    String basePrice = par.getString("basePrice");
                    /*basePrice = basePrice.substring(0, basePrice.length() - 2);
                    Long base = Long.parseLong(basePrice);
                    basePrice = String.valueOf(base);*/
                    String sellPrice = par.getString("sellPrice");
                    /*sellPrice = sellPrice.substring(0, sellPrice.length() - 2);
                    Long sell = Long.parseLong(sellPrice);
                    sellPrice = String.valueOf(sell);*/
                    String fee = par.getString("fee");
                    /*fee = fee.substring(0, fee.length() - 2);
                    Long feeL = Long.parseLong(fee);
                    fee = String.valueOf(feeL);*/

                    JsonObject isi2 = new JsonObject();
                    isi2.addProperty("productId", productId);
                    isi2.addProperty("productName", productName);
                    isi2.addProperty("productDesc", productDesc);
                    isi2.addProperty("operator", operator);
                    isi2.addProperty("basePrice", basePrice);
                    isi2.addProperty("sellPrice", sellPrice);
                    isi2.addProperty("fee", fee);
                    isi2.addProperty("type", type);

                    body.add("body" + i, isi2);

                }
            }

            FinancialApplication.getSysParam().set("download", body.toString());

            return 0;

        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /*public static int updateProductDataById(String tipe, ProductData prdData) {

        ProductData productData = new ProductData();
        String data = FinancialApplication.getSysParam().get("download");

        if (TextUtils.isEmpty(data))return -1;

        try {
            JSONObject dat = new JSONObject(data);

            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String type = par.getString("type").trim();

                String productId = par.getString("productId");
                //Log.d("teg", "type : "+type+" param : "+tipe+"| operator : "+operator+" param : "+oprator);

                if (type.equalsIgnoreCase(tipe) && productId.equalsIgnoreCase(prdData.getProductId())) {
                    Log.d("-->", "type : "+type+" param : "+tipe+"| productId : "+productId+" param : "+prdData.getProductId());

                    String operator = par.getString("operator").trim();
                    String productName = par.getString("productName").trim();
                    String productDesc = par.getString("productDesc").trim();

                    String basePrice = par.getString("basePrice");
                    basePrice = basePrice.substring(0, basePrice.length() - 2);
                    Long base = Long.parseLong(basePrice);
                    basePrice = String.valueOf(base);
                    String sellPrice = par.getString("sellPrice");
                    sellPrice = sellPrice.substring(0, sellPrice.length() - 2);
                    Long sell = Long.parseLong(sellPrice);
                    sellPrice = String.valueOf(sell);
                    String fee = par.getString("fee");
                    fee = fee.substring(0, fee.length() - 2);
                    Long feeL = Long.parseLong(fee);
                    fee = String.valueOf(feeL);

                    productData = new ProductData(productId, type, productName, productDesc, operator, basePrice, sellPrice, fee);
                    Log.i("teg", "-->" + productData.toString());
                    return 0;

                }
            }

        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }*/

    public static List<ProductData> getAllProductDataByPrefix(String prefix) {

        List<ProductData> list = new ArrayList<>();
        String prefixStr = FinancialApplication.getSysParam().get("prefix");
        //Log.i("teg", "prefix : " + prefixStr);

        if (TextUtils.isEmpty(prefixStr)) return null;

        try {

            JSONObject prefixJson = new JSONObject(prefixStr);
            for (int i = 0; i < prefixJson.length(); i++) {
                JSONObject par = prefixJson.getJSONObject("body" + i);
                String nopref = par.getString("prefix");

                if (nopref.contains(prefix)) {
                    String operator = par.getString("operator");
                    list.addAll(getAllProductDataByType(PULSA, operator));
                }

            }
        } catch (JSONException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return list;
    }



    public static ProductData getProductById(String prodId) {
        JSONObject dat;
        String data = FinancialApplication.getSysParam().get("download");

        if (TextUtils.isEmpty(data)) return null;

        try {
            dat = new JSONObject(data);

            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String productId = par.getString("productId").trim();

                if (productId.equalsIgnoreCase(prodId)) {
                    String pId = par.getString("productId");
                    String productName = par.getString("productName").trim();
                    String type = par.getString("type").trim();
                    String productDesc = par.getString("productDesc").trim();
                    String operator = par.getString("operator");
                    String basePrice = par.getString("basePrice");
                    basePrice = basePrice.substring(0, basePrice.length() - 2);
                    Long base = Long.parseLong(basePrice);
                    basePrice = String.valueOf(base);
                    String sellPrice = par.getString("sellPrice");
                    sellPrice = sellPrice.substring(0, sellPrice.length() - 2);
                    Long sell = Long.parseLong(sellPrice);
                    sellPrice = String.valueOf(sell);
                    String fee = par.getString("fee");
                    fee = fee.substring(0, fee.length() - 2);
                    Long feeL = Long.parseLong(fee);
                    fee = String.valueOf(feeL);
                    operator = operator.toUpperCase().trim();
                    return new ProductData(pId, type, productName, productDesc, operator, basePrice, sellPrice, fee);
                }
            }

        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }











}
