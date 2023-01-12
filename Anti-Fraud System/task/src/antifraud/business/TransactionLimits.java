package antifraud.business;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
public class TransactionLimits {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private long allowedLimit;

    @Column
    private long manualLimit;

    @Column
    private LocalDateTime date;

    public TransactionLimits(long allowedLimit, long manualLimit, LocalDateTime localDateTime) {
        this.allowedLimit = allowedLimit;
        this.manualLimit = manualLimit;
        this.date = localDateTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
