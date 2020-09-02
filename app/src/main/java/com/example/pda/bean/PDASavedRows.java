package com.example.pda.bean;

public class PDASavedRows {
    private String scancode;
    private String iNum;
    private String bTrue;

    public String getiNum() {
        return iNum;
    }

    public void setiNum(String iNum) {
        this.iNum = iNum;
    }

    public String getScancode() {
        return scancode;
    }

    public void setScancode(String scancode) {
        this.scancode = scancode;
    }

    public String getbTrue() {
        return bTrue;
    }

    public void setbTrue(String bTrue) {
        this.bTrue = bTrue;
    }
}
