package antifraud.business;

import antifraud.persistence.StolenCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StolenCardService {

    private final StolenCardRepository stolenCardRepository;

    @Autowired
    public StolenCardService(StolenCardRepository stolenCardRepository) {
        this.stolenCardRepository = stolenCardRepository;
    }

    public List<StolenCard> findAll() {
        return stolenCardRepository.findAll();
    }

    public StolenCard findByNumber(String number) {
        return stolenCardRepository.findStolenCardByNumber(number);
    }

    public StolenCard save(StolenCard stolenCard) {
        return stolenCardRepository.save(stolenCard);
    }

    public void delete(StolenCard stolenCard) {
        stolenCardRepository.delete(stolenCard);
    }
}
