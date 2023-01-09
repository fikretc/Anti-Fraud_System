package antifraud.business;

import antifraud.AntiFraudController;
import antifraud.persistence.TransactionHistoryRepository;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Component
public class TransactionService {
    public static final String REJECT_REASON_NUMBER = "card-number";
    public static final String REJECT_REASON_IP = "ip";
    public static final String REJECT_REASON_AMOUNT = "amount";

    public static final String REJECT_REASON_IP_CORRELATION = "ip-correlation";

    public static final String REJECT_REASON_REGION_CORRELATION = "region-correlation";

    public static final String REJECT_REASON_NONE = "none";

    public static final String REJECT_REASON_SEPARATOR = ", ";

    static final int CORRELATION_LIMIT_IP = 3;
    static final int CORRELATION_LIMIT_REGION =3;

    @Autowired
    SuspiciousIpService suspiciousIpService;
    @Autowired
    StolenCardService stolenCardService;
    @Autowired
    TransactionHistoryRepository transactionHistoryRepository;

    private static final Logger logger = LogManager
            .getLogger(TransactionService.class);

    public Map<String, Set<String>> findFraudByNumber (Amount amountToValidate ) {
        List<Amount> amountList = transactionHistoryRepository
                .findAllByNumber(amountToValidate.getNumber());
        amountList.sort((amount1, amount2) -> amount2.getDate().compareTo(amount1.getDate()));
        List<Amount> amountListPreviousHour = amountList.stream()
                //.filter(amount -> (amount.getDate().isAfter(amountToValidate.getDate().minusHours(1l))))
                .filter(amount -> amount.getDate()
                        .compareTo(amountToValidate.getDate().minusHours(1l))>0)
                .filter(amount -> (amount.getDate().compareTo(amountToValidate.getDate())<0))
                .collect(Collectors.toList());
        logger.debug("amountListPreviousHour "
                + amountListPreviousHour.stream()
                .map(u -> "\n" + u.getNumber() + " " +u.getRegion() + " "
                        + u.getIp() + " " + u.getDate()).toList());

        Set<String> distinctIpSet = amountListPreviousHour.stream()
                .map(amount -> amount.getIp()).collect(Collectors.toSet());
        distinctIpSet.add(amountToValidate.getIp());

        logger.debug("distinctIpSet: "
                + distinctIpSet.stream()
                .map(u -> "\n" + u)
                .toList());

        Set<String> distinctRegionSet = amountListPreviousHour.stream()
                .map(amount -> amount.getRegion()).collect(Collectors.toSet());
        distinctRegionSet.add(amountToValidate.getRegion());

        logger.debug("distinctRegionSet: "
                + distinctRegionSet.stream()
                .map(u -> "\n" + u)
                .toList());

        Map<String, Set<String>> resultSet = new HashMap<>(2);
        resultSet.put("regions", distinctRegionSet);
        resultSet.put("ips", distinctIpSet);
        return resultSet;
    }

    public TransactionResult evaluateTransaction(Amount amount ) {

        transactionHistoryRepository.save(amount);

        String result;
        String info = REJECT_REASON_NONE;

        SortedSet<String> infoSet = new TreeSet<>();
        Set<String> resultSet = new HashSet<>();

        if ( amount.processingType().equals(Amount.PROHIBITED)) {
           infoSet.add (REJECT_REASON_AMOUNT);
        } else if (amount.processingType().equals(Amount.MANUAL_PROCESSING)) {
            info = REJECT_REASON_AMOUNT;
        }

        if (suspiciousIpService.findByIp(amount.getIp()) != null) {
            infoSet.add (REJECT_REASON_IP);
            resultSet.add(Amount.PROHIBITED);
        }

        if (stolenCardService.findByNumber(amount.getNumber()) != null) {
            infoSet.add (REJECT_REASON_NUMBER);
            resultSet.add(Amount.PROHIBITED);
        }

        Map<String, Set<String>> historyMap = findFraudByNumber( amount );
        int nIp = historyMap.get("ips").size();
        if ( nIp > CORRELATION_LIMIT_IP) {
            resultSet.add(Amount.PROHIBITED);
            infoSet.add(REJECT_REASON_IP_CORRELATION);
        } else if (nIp == CORRELATION_LIMIT_IP) {
            resultSet.add(Amount.MANUAL_PROCESSING);
            infoSet.add(REJECT_REASON_IP_CORRELATION);
        }

        int nRegion = historyMap.get("regions").size();
        if ( nRegion > CORRELATION_LIMIT_REGION) {
            resultSet.add(Amount.PROHIBITED);
            infoSet.add(REJECT_REASON_REGION_CORRELATION);
        } else if (nRegion == CORRELATION_LIMIT_REGION) {
            resultSet.add(Amount.MANUAL_PROCESSING);
            infoSet.add(REJECT_REASON_REGION_CORRELATION);
        }

        if (!infoSet.isEmpty()) {
            ArrayList<String> strings = new ArrayList<>(infoSet);
            info = String.join (REJECT_REASON_SEPARATOR, strings);
        }

        if (resultSet.contains(Amount.PROHIBITED)){
            result = Amount.PROHIBITED;
        } else if (resultSet.contains(Amount.MANUAL_PROCESSING)) {
            result = Amount.MANUAL_PROCESSING;
        } else {
            result = amount.processingType();
        }
        return new TransactionResult(result, info);
    }
}
