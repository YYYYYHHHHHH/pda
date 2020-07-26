package com.example.pda.commpont;

import java.util.Objects;

public class MyContent {
    private String content;

    public MyContent(String content) {
        this.content = content;
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

