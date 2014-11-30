package gr.modissense.core;


import com.google.gson.annotations.Expose;

public class ModiResult {
    @Expose
    private boolean result=false;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ModiResult{" +
                "result=" + result +
                '}';
    }
}
