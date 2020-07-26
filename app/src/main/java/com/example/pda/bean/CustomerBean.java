package com.example.pda.bean;

public class CustomerBean {
    private String custId;
    private String Column1;

    @Override
    public String toString() {
        return Column1;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getColumn1() {
        return Column1;
    }

    public void setColumn1(String column1) {
        Column1 = column1;
    }
}
