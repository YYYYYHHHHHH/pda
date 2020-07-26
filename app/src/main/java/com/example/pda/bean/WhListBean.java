package com.example.pda.bean;

import java.util.List;

public class WhListBean {
    private String 仓库列表;
    private List<WhBean> Rows;

    public String get仓库列表() {
        return 仓库列表;
    }

    public void set仓库列表(String 仓库列表) {
        this.仓库列表 = 仓库列表;
    }

    public List<WhBean> getRows() {
        return Rows;
    }

    public void setRows(List<WhBean> rows) {
        Rows = rows;
    }

}

