package com.pax.pay.utils;

import android.util.Log;

import com.pax.pay.app.FinancialApplication;
import com.pax.up.bjb.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ResponseCode {
    public static final String TAG = "ResponseCode";

    private String code;
    private String category;
    private String message;
    private HashMap<String, ResponseCode> map;
    private static ResponseCode rcCode;

    private ResponseCode() {

        if (map == null)
            map = new HashMap<String, ResponseCode>();
    }

    public static ResponseCode getInstance() {
        if (rcCode == null) {
            rcCode = new ResponseCode();
        }
        return rcCode;
    }

    /**
     * init方法必须调用， 一般放在应用启动的时候
     * 
     * @param context
     */
    public void init(InputStream is) {
        try {
            DocumentBuilderFactory docFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuidler = null;
            Document doc = null;
            docBuidler = docFact.newDocumentBuilder();
            doc = docBuidler.parse(is);
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("response");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                String localCode = element.getAttribute("code");
                String category = element.getAttribute("category");
                String message = element.getAttribute("message");
                ResponseCode rspCode = new ResponseCode(localCode, category, message);
                map.put(localCode, rspCode);
            }
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "", e);
        } catch (SAXException e) {
            Log.e(TAG, "", e);
        }
    }

    public ResponseCode(String code, String category, String message) {
        this.code = code;
        this.category = category;
        this.message = message;
    }

    public ResponseCode parse(String code) {
        ResponseCode rc = map.get(code);
        if (rc == null)
            return new ResponseCode(code, "F", FinancialApplication.getAppContext().getString(R.string
                    .err_undefine_info));
        return rc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
