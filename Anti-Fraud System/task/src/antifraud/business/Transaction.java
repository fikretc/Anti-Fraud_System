package antifraud.business;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Entity
@Table
@JsonPropertyOrder({ "id", "amount", "ip", "number", "region", "date", "result", "feedback" })
public class Transaction {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private long amount;

    @Column
    private String ip;

    @Column
    private String number;

    @Column
    private String region;

    @Column
    private LocalDateTime date;

    @Column
    private String result;

    @Column
    private String feedback;

    @Column
    private String info;

    @Column
    private long allowedLimit;

    @Column
    private long manualLimit;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    @JsonProperty("transactionId")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public boolean validate(){
        return this.amount > 0l;
    }

    public long getAllowedLimit() {
        return allowedLimit;
    }

    public void setAllowedLimit(long allowedLimit) {
        this.allowedLimit = allowedLimit;
    }

    public long getManualLimit() {
        return manualLimit;
    }

    public void setManualLimit(long manualLimit) {
        this.manualLimit = manualLimit;
    }

    public String toDebugString() {
        return ("\nId: " + this.getId() + " " + this.getAmount() + " " + this.getNumber() + " " + this.getIp()
                + " " + this.getRegion() + " " + this.getResult() + " " + this.getFeedback()
                + "\n" + this.getInfo() + " " + this.getAllowedLimit() + " " + this.getManualLimit());
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    @JsonPropertyOrder({ "transactionId", "amount", "ip", "number", "region", "date", "result", "feedback" })
    public class TransactionView {
        private static final String DATE_FORMATTER= "yyyy-MM-ddTHH:mm:ss";

        public Long getAmount() {
            return Transaction.this.amount;
        }

        public String getIp() {
            return Transaction.this.ip;
        }

        public String getNumber() {
            return Transaction.this.number;
        }
        public String getRegion() {
            return Transaction.this.region;
        }
        public String getDate() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);
            return Transaction.this.getDate().format(formatter);
        }

        public String getResult() {
            return Transaction.this.getResult();
        }

        public String getFeedback() {
            return Transaction.this.getFeedback();
        }
    }
}
