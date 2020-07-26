package com.example.pda.bean;

import java.util.List;

public class CustomerListBean {
    private String 客户列表;
    private List<CustomerBean> Rows;

    public String get客户列表() {
        return 客户列表;
    }

    public void set客户列表(String 客户列表) {
        this.客户列表 = 客户列表;
    }

    public List<CustomerBean> getRows() {
        return Rows;
    }

    public void setRows(List<CustomerBean> rows) {
        Rows = rows;
    }
}
