package antifraud.business;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table
public class StolenCard {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    private String number;

    public StolenCard() {
    }

    public StolenCard(String number) {
        this.number = number;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
