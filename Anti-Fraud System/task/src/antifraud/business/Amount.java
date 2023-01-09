package antifraud.business;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table
public class Amount {

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


    public static final long ALLOWED_LIMIT = 200l;
    public static final long PROHIBITED_LIMIT = 1500l;

    public static final String ALLOWED = "ALLOWED";
    public static final String MANUAL_PROCESSING = "MANUAL_PROCESSING";
    public static final String PROHIBITED = "PROHIBITED";

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

    public boolean validate(){
        return this.amount > 0l;
    }

    public String processingType() {

        if (this.amount <= ALLOWED_LIMIT) {
            return ALLOWED;
        } else if (this.amount <= PROHIBITED_LIMIT) {
            return MANUAL_PROCESSING;
        }
        return PROHIBITED;
    }
    @JsonPropertyOrder({ "amount", "ip", "number", "region", "date" })
    public class AmountView {
        private static final String DATE_FORMATTER= "yyyy-MM-ddTHH:mm:ss";

        public Long getAmount() {
            return Amount.this.amount;
        }

        public String getIp() {
            return Amount.this.ip;
        }

        public String getNumber() {
            return Amount.this.number;
        }
        public String getRegion() {
            return Amount.this.region;
        }
        public String getDate() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);
            return Amount.this.getDate().format(formatter);
        }
    }
}
