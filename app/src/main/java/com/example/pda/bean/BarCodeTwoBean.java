package com.example.pda.bean;

public class BarCodeTwoBean {
    private String Status;
    private String invClass;
    private String invName;
    private String Msg;
    private String custId;
    private String custName;

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getInvClass() {
        return invClass;
    }

    public void setInvClass(String invClass) {
        this.invClass = invClass;
    }

    public String getInvName() {
        return invName;
    }

    public void setInvName(String invName) {
        this.invName = invName;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }
}
