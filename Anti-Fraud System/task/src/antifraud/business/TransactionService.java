package antifraud.business;

import antifraud.persistence.TransactionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TransactionService {
    public static final String REJECT_REASON_NUMBER = "card-number";
    public static final String REJECT_REASON_IP = "ip";
    public static final String REJECT_REASON_AMOUNT = "amount";

    public static final String REJECT_REASON_IP_CORRELATION = "ip-correlation";

    public static final String REJECT_REASON_REGION_CORRELATION = "region-correlation";

    public static final String REJECT_REASON_NONE = "none";

    public static final String REJECT_REASON_SEPARATOR = ", ";

    @Autowired
    SuspiciousIpService suspiciousIpService;
    @Autowired
    StolenCardService stolenCardService;
    @Autowired
    TransactionHistoryRepository transactionHistoryRepository;

    public TransactionResult evaluateTransaction(Amount amount) {

        String result = amount.processingType();
        String info = REJECT_REASON_NONE;

        SortedSet<String> infoSet = new TreeSet<>();

        if ( result.equals(Amount.PROHIBITED)) {
           infoSet.add (REJECT_REASON_AMOUNT);
        } else if (result.equals(Amount.MANUAL_PROCESSING)) {
            info = REJECT_REASON_AMOUNT;
        }

        if (suspiciousIpService.findByIp(amount.getIp()) != null) {
            infoSet.add (REJECT_REASON_IP);
            result = Amount.PROHIBITED;
        }

        if (stolenCardService.findByNumber(amount.getNumber()) != null) {
            infoSet.add (REJECT_REASON_NUMBER);
            result = Amount.PROHIBITED;
        }

        if (!infoSet.isEmpty()) {
            ArrayList<String> strings = new ArrayList<>(infoSet);
            info = String.join (REJECT_REASON_SEPARATOR, strings);
        }
        return new TransactionResult(result, info);
    }
}
