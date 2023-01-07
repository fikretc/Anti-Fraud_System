package antifraud.persistence;

import antifraud.business.StolenCard;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StolenCardRepository extends CrudRepository <StolenCard, Long> {
    StolenCard findStolenCardByNumber (String number);
    List<StolenCard> findAll();

}
