package antifraud.persistence;

import antifraud.business.TransactionLimits;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TransactionLimitsRepository extends CrudRepository <TransactionLimits, Long> {
    TransactionLimits save(TransactionLimits transactionLimits);

    List<TransactionLimits> findAll();
}
