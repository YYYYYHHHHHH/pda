package com.example.pda.bean;

public class BarCodeBean {
    private String Status;
    private String Msg;
    private int proId;
    private boolean Packaged;

    public boolean isPackaged() {
        return Packaged;
    }

    public void setPackaged(boolean packaged) {
        Packaged = packaged;
    }

    public int getProId() {
        return proId;
    }

    public void setProId(int proId) {
        this.proId = proId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }
}
