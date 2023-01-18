package antifraud.business;

import antifraud.persistence.TransactionRepository;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static antifraud.business.TransactionLimitsService.*;
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
    TransactionRepository transactionRepository;

    @Autowired
    TransactionLimitsService transactionLimitsService;

    private static final Logger logger = LogManager
            .getLogger(TransactionService.class);

    public List<Transaction> findAll() {
        List<Transaction> list = transactionRepository.findAll();
        list.sort((tx1, tx2) -> (int) Math.signum(tx1.getId()-tx2.getId()));
        return list;

    }

    public List<Transaction> findAllByNumber(String number ) {
        List<Transaction> list = transactionRepository.findAllByNumber( number );
        list.sort((tx1, tx2) -> (int) Math.signum(tx1.getId()-tx2.getId()));
        return list;

    }

    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public Map<String, Set<String>> findFraudByNumber (Transaction transactionToValidate) {
        List<Transaction> transactionList = transactionRepository
                .findAllByNumber(transactionToValidate.getNumber());
        List<Transaction> transactionListPreviousHour = transactionList.stream()
                //.filter(amount -> (amount.getDate().isAfter(amountToValidate.getDate().minusHours(1l))))
                .filter(amount -> amount.getDate()
                        .compareTo(transactionToValidate.getDate().minusHours(1l))>=0)
                .filter(amount -> (amount.getDate().compareTo(transactionToValidate.getDate())<=0))
                .collect(Collectors.toList());
        transactionListPreviousHour.sort((tx1, tx2) -> (int) Math.signum(tx2.getId()-tx1.getId()));

        logger.debug("amountListPreviousHour "
                + transactionListPreviousHour.stream()
                .map(u -> "\n" + u.getNumber() + " " +u.getRegion() + " "
                        + u.getIp() + " " + u.getDate()).toList());

        Set<String> distinctIpSet = transactionListPreviousHour.stream()
                .map(amount -> amount.getIp()).collect(Collectors.toSet());
        distinctIpSet.add(transactionToValidate.getIp());

        logger.debug("distinctIpSet: "
                + distinctIpSet.stream()
                .map(u -> "\n" + u)
                .toList());

        Set<String> distinctRegionSet = transactionListPreviousHour.stream()
                .map(amount -> amount.getRegion()).collect(Collectors.toSet());
        distinctRegionSet.add(transactionToValidate.getRegion());

        logger.debug("distinctRegionSet: "
                + distinctRegionSet.stream()
                .map(u -> "\n" + u)
                .toList());

        Map<String, Set<String>> resultSet = new HashMap<>(2);
        resultSet.put("regions", distinctRegionSet);
        resultSet.put("ips", distinctIpSet);
        return resultSet;
    }

    public boolean validate(Transaction transaction){
        return (transaction.getAmount() > 0l
                && ParameterChecker.isValidIPAddress(transaction.getIp())
                && ParameterChecker.checkLuhn(transaction.getNumber()));
    }


    public String processingType(Transaction transaction) {

        if (transaction.getAmount() <= transaction.getAllowedLimit()) {
            return ALLOWED;
        } else if (transaction.getAmount() <= transaction.getManualLimit()) {
            return MANUAL_PROCESSING;
        }
        return PROHIBITED;
    }

    public TransactionResult evaluateTransaction(Transaction transaction) {

        String result;
        String info = REJECT_REASON_NONE;

        SortedSet<String> infoSet = new TreeSet<>();
        Set<String> resultSet = new HashSet<>();
        TransactionLimits transactionLimits = transactionLimitsService.findTransactionLimits();
        transaction.setAllowedLimit(transactionLimits.getAllowedLimit());
        transaction.setManualLimit(transactionLimits.getManualLimit());

        if ( processingType( transaction).equals(PROHIBITED)) {
           infoSet.add (REJECT_REASON_AMOUNT);
        } else if (processingType(transaction).equals(MANUAL_PROCESSING)) {
            info = REJECT_REASON_AMOUNT;
        }

        if (suspiciousIpService.findByIp(transaction.getIp()) != null) {
            infoSet.add (REJECT_REASON_IP);
            resultSet.add(PROHIBITED);
        }

        if (stolenCardService.findByNumber(transaction.getNumber()) != null) {
            infoSet.add (REJECT_REASON_NUMBER);
            resultSet.add(PROHIBITED);
        }

        Map<String, Set<String>> historyMap = findFraudByNumber(transaction);
        int nIp = historyMap.get("ips").size();
        if ( nIp > CORRELATION_LIMIT_IP) {
            resultSet.add(PROHIBITED);
            infoSet.add(REJECT_REASON_IP_CORRELATION);
        } else if (nIp == CORRELATION_LIMIT_IP) {
            resultSet.add(MANUAL_PROCESSING);
            infoSet.add(REJECT_REASON_IP_CORRELATION);
        }

        int nRegion = historyMap.get("regions").size();
        if ( nRegion > CORRELATION_LIMIT_REGION) {
            resultSet.add(PROHIBITED);
            infoSet.add(REJECT_REASON_REGION_CORRELATION);
        } else if (nRegion == CORRELATION_LIMIT_REGION) {
            resultSet.add(MANUAL_PROCESSING);
            infoSet.add(REJECT_REASON_REGION_CORRELATION);
        }

        if (!infoSet.isEmpty()) {
            ArrayList<String> strings = new ArrayList<>(infoSet);
            info = String.join (REJECT_REASON_SEPARATOR, strings);
        }

        if (resultSet.contains(PROHIBITED)){
            result = PROHIBITED;
        } else if (resultSet.contains(MANUAL_PROCESSING)) {
            result = MANUAL_PROCESSING;
        } else {
            result = processingType(transaction);
        }
        return new TransactionResult(result, info);
    }
}
