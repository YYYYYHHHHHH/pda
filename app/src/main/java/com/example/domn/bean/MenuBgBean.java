package com.example.domn.bean;

import com.example.domn.ListActivity;

import java.util.List;

public class MenuBgBean {
    private String PDAMenu;
    private List<MenuBean> Rows;

    public String getPDAMenu() {
        return PDAMenu;
    }

    public void setPDAMenu(String PDAMenu) {
        this.PDAMenu = PDAMenu;
    }

    public List<MenuBean> getRows() {
        return Rows;
    }

    public void setRows(List<MenuBean> rows) {
        Rows = rows;
    }
}
