package com.example.pda.bean;

public class WhBean {
        private String whId;
        private String whName;

        public String getWhId() {
            return whId;
        }

        public void setWhId(String whId) {
            this.whId = whId;
        }

        public String getWhName() {
            return whName;
        }

        public void setWhName(String whName) {
            this.whName = whName;
        }

    @Override
    public String toString() {
        return whName;
    }
}
