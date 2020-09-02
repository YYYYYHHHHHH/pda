package com.example.pda.bean;

public class PDASavedBean {
    private String 条码列表;
    private PDASavedRows[] Rows;

    public String get条码列表() {
        return 条码列表;
    }

    public void set条码列表(String 条码列表) {
        this.条码列表 = 条码列表;
    }

    public PDASavedRows[] getRows() {
        return Rows;
    }

    public void setRows(PDASavedRows[] rows) {
        Rows = rows;
    }
}
