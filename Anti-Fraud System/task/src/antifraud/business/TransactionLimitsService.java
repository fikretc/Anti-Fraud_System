package antifraud.business;

import antifraud.persistence.TransactionLimitsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionLimitsService {

    private static final Logger logger = LogManager.getLogger(TransactionLimitsService.class);

    public static final long ALLOWED_LIMIT = 200l;
    public static final long MANUAL_LIMIT = 1500l;

    public static final String ALLOWED = "ALLOWED";
    public static final String MANUAL_PROCESSING = "MANUAL_PROCESSING";
    public static final String PROHIBITED = "PROHIBITED";

    public static final List<String> TRANSACTION_RESULT_LIST =
            Arrays.asList(new String[] {ALLOWED, MANUAL_PROCESSING, PROHIBITED});
    @Autowired
    TransactionLimitsRepository transactionLimitsRepository;


    public TransactionLimits save (TransactionLimits transactionLimits) {
        return transactionLimitsRepository.save(transactionLimits);
    }

    public TransactionLimits findTransactionLimits() {
        List<TransactionLimits> transactionLimitsList = transactionLimitsRepository.findAll();
        if(transactionLimitsList.size() == 0) {
            return new TransactionLimits(ALLOWED_LIMIT, MANUAL_LIMIT, LocalDateTime.now());
        }
        transactionLimitsList.sort((t1, t2 ) -> t2.getDate().compareTo(t1.getDate())) ;
        TransactionLimits transactionLimits = transactionLimitsList.get(0);
        logger.debug(" TransactionLimits history:" +
                transactionLimitsList.stream()
                        .map(u -> "\nA " + u.getAllowedLimit() + " M " + u.getManualLimit() + " D " + u.getDate())
                        .collect(Collectors.toList()));

        return transactionLimits;
    }

    private long increaseLimit(long current_limit, long value_from_transaction) {
        long new_limit = (long)Math.ceil(0.8 * current_limit + 0.2 * value_from_transaction);
        logger.debug(" Limit " + current_limit + " increased by value " + value_from_transaction + " to " + new_limit);
        return new_limit;
    }

    private long decreaseLimit(long current_limit, long value_from_transaction) {
        long new_limit = (long)Math.ceil(0.8 * current_limit - 0.2 * value_from_transaction);
        logger.debug(" Limit " + current_limit + " decreased by value " + value_from_transaction + " to " + new_limit);
        return new_limit;
    }

    public boolean processFeedback(Transaction transaction, TransactionFeedback transactionFeedback) {
        TransactionLimits transactionLimits = new TransactionLimits(transaction.getAllowedLimit(),
                transaction.getManualLimit(), LocalDateTime.now()); //according to feedback

        if (transaction.getResult().equals(ALLOWED)) {
            if ( transactionFeedback.getFeedback().equals(MANUAL_PROCESSING)) {
                transactionLimits.setAllowedLimit(
                        decreaseLimit(transactionLimits.getAllowedLimit(), transaction.getAmount()));
            } else if (transactionFeedback.getFeedback().equals(PROHIBITED)) {
                transactionLimits.setAllowedLimit(
                        decreaseLimit(transactionLimits.getAllowedLimit(), transaction.getAmount()));
                transactionLimits.setManualLimit(
                        decreaseLimit(transactionLimits.getManualLimit(), transaction.getAmount()));
            }
        } else if (transaction.getResult().equals(MANUAL_PROCESSING)) {
            if (transactionFeedback.getFeedback().equals(ALLOWED)) {
                transactionLimits.setAllowedLimit(
                        increaseLimit(transactionLimits.getAllowedLimit(), transaction.getAmount()));
            } else if (transactionFeedback.getFeedback().equals(PROHIBITED)) {
                transactionLimits.setManualLimit(
                        decreaseLimit(transactionLimits.getManualLimit(), transaction.getAmount()));
            }
        } else if (transaction.getResult().equals(PROHIBITED)) {
            if (transactionFeedback.getFeedback().equals(ALLOWED)) {
                transactionLimits.setAllowedLimit(
                        increaseLimit(transactionLimits.getAllowedLimit(), transaction.getAmount()));
                transactionLimits.setManualLimit(
                        increaseLimit(transactionLimits.getManualLimit(), transaction.getAmount()));
            } else if (transactionFeedback.getFeedback().equals(MANUAL_PROCESSING)) {
                transactionLimits.setManualLimit(
                        increaseLimit(transactionLimits.getManualLimit(), transaction.getAmount()));
            }
        }
        save(transactionLimits);
        return true;
    }
}
