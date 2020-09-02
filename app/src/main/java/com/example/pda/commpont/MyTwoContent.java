package com.example.pda.commpont;

import java.util.Objects;

public class MyTwoContent {
    private String content;
    private Boolean isGroup = true;
    private String invClass;
    private int proId;

    public int getProId() {
        return proId;
    }

    public void setProId(int proId) {
        this.proId = proId;
    }

    public MyTwoContent(String content) {
        this.content = content;
    }

    public MyTwoContent(String content, int proId) {
        this.content = content;
        this.proId = proId;
    }

    public MyTwoContent(String content, String invClass) {
        this.content = content;
        this.invClass = invClass;
    }

    public MyTwoContent(String content, String invClass, int proId) {
        this.content = content;
        this.invClass = invClass;
        this.proId = proId;
    }

    public String getInvClass() {
        return invClass;
    }

    public void setInvClass(String invClass) {
        this.invClass = invClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyTwoContent that = (MyTwoContent) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getGroup() {
        return isGroup;
    }

    public void setGroup(Boolean group) {
        isGroup = group;
    }
}
