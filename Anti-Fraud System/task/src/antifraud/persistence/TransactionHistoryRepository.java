package antifraud.persistence;

import antifraud.business.Amount;
import org.springframework.data.repository.CrudRepository;

public interface TransactionHistoryRepository extends CrudRepository<Amount, Long> {
}
