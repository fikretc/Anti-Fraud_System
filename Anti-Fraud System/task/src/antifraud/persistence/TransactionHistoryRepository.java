package antifraud.persistence;

import antifraud.business.Amount;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransactionHistoryRepository extends CrudRepository<Amount, Long> {
    List<Amount> findAllByNumber(String number);

}
