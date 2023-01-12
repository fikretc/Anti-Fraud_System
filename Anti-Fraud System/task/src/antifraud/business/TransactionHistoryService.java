package antifraud.business;

import antifraud.AntiFraudController;
import antifraud.persistence.TransactionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
public class TransactionHistoryService {
    @Autowired
    TransactionHistoryRepository transactionHistoryRepository;


    public List<Amount> findAll() {
        List<Amount> list = transactionHistoryRepository.findAll();
        list.sort((amount1, amount2 ) -> amount1.getDate().compareTo(amount2.getDate()));
        return list;

    }

    public List<Amount> findAllByNumber( String number ) {
        List<Amount> list = transactionHistoryRepository.findAllByNumber( number );
        list.sort((amount1, amount2 ) -> amount1.getDate().compareTo(amount2.getDate()));
        return list;

    }

    public void save(Amount amount) {
        transactionHistoryRepository.save(amount);
    }


}
