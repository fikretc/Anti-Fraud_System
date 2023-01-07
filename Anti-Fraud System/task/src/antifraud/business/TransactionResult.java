package antifraud.business;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder ({"result", "info"})
public class TransactionResult {
    @JsonProperty
    String result;
    @JsonProperty
    String info;

    public TransactionResult( String result, String info) {
        this.result = result;
        this.info = info;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
