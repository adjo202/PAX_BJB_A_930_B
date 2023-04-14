package com.pax.pay.trans.model;

public class OptionModel {

    private String id;

    private String content;

    private Object object;

    public OptionModel(String id, String content) {
        super();
        this.id = id;
        this.content = content;
    }

    public OptionModel(String id, String content, Object object) {
        super();
        this.id = id;
        this.content = content;
        this.object = object;
    }


    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
