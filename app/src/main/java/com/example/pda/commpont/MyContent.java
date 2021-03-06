package com.example.pda.commpont;

import java.util.Objects;

public class MyContent {
    private String content;
    private String bTrue;
    private String iNum;
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

    public String getiNum() {
        return iNum;
    }

    public void setiNum(String iNum) {
        this.iNum = iNum;
    }

    public String getbTrue() {
        return bTrue;
    }

    public void setbTrue(String bTrue) {
        this.bTrue = bTrue;
    }

    public MyContent(String content, String bTrue) {
        this.content = content;
        this.bTrue = bTrue;
    }

    public MyContent(String content, int proId, boolean packaged) {
        this.content = content;
        this.proId = proId;
        Packaged = packaged;
    }

    public MyContent(String content, String bTrue, String iNum) {
        this.content = content;
        this.bTrue = bTrue;
        this.iNum = iNum;
    }

    public MyContent(String content) {
        this.content = content;
    }

    public MyContent(String content, int proId) {
        this.content = content;
        this.proId = proId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyContent myContent = (MyContent) o;
        return content.equals(myContent.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

}

