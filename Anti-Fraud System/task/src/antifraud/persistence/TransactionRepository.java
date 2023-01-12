package antifraud.persistence;

import antifraud.business.Transaction;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    List<Transaction> findAllByNumber(String number);

    List<Transaction> findAll();

    Transaction findById(long id);

    Transaction save(Transaction transaction);

}
