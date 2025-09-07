package com.nkm.ar.core.java.network;

public class OcrResponse {
    public boolean success;
    public String message;
    public String error;
    public float confidence;

    public OcrResponse() {}
    public OcrResponse(boolean success, String message, String error) {
        this.success = success;
        this.message = message;
        this.error = error;

    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean s) { success = s; }
    public String getMessage() { return message; }
    public void setMessage(String m) { message = m; }
    public String getError() { return error; }
    public void setError(String e) { error = e; }
}
