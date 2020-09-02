package com.example.pda.bean;

public class GetBarDetailsBean {
    private String 条码明细;
    private GetBarDetailsRows[] Rows;

    public String get条码明细() {
        return 条码明细;
    }

    public void set条码明细(String 条码明细) {
        this.条码明细 = 条码明细;
    }

    public GetBarDetailsRows[] getRows() {
        return Rows;
    }

    public void setRows(GetBarDetailsRows[] rows) {
        Rows = rows;
    }
}
