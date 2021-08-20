package com.bitmap.hikvideoplugin.http;

/**
 * Create By axd On 2021/8/18.
 * Email 43229097@qq.com
 * Describe：
 */
public class RtspBean {

    private String code;
    private String message;
    private Data data;

    public void setCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Data data) {
        this.data = data;
    }
    public Data getData() {
        return data;
    }

    public class Data {
        private String url;
        public void setUrl(String url) {
            this.url = url;
        }
        public String getUrl() {
            return url;
        }

    }

}

