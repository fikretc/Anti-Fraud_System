package antifraud;

import antifraud.business.*;
import antifraud.persistence.TransactionRepository;
import antifraud.security.IAuthenticationFacade;
import antifraud.security.SecurityParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Controller
public class AntiFraudController {

    @Autowired
    UserParametersService userParametersService;
    @Autowired
    StolenCardService stolenCardService;
    @Autowired
    SuspiciousIpService suspiciousIpService;

    @Autowired
    TransactionService transactionService;

    private static final Logger logger = LogManager.getLogger(AntiFraudController.class);

    @Autowired
    private IAuthenticationFacade authenticationFacade;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    TransactionLimitsService transactionLimitsService;

    public String currentUserName() {
        final Authentication authentication = authenticationFacade.getAuthentication();
        return authentication.getName();
    }

    @PostMapping(value="/api/antifraud/transaction", produces="application/json")
    public ResponseEntity processAmount(@RequestBody Transaction transaction) {
        UserParameters checkUser = userParametersService
                .findByUsername(currentUserName());

        if (checkUser.getRole().equals(SecurityParams.MERCHANT)) {

            if (checkUser.getStatus().equals(SecurityParams.LOCKED)) {
                logger.debug("PostMapping /api/antifraud/transaction1 "
                        + checkUser.getUsername() + " " + checkUser.getStatus());
                return ResponseEntity.status(401).body("User status LOCKED");
            }
            if (transactionService.validate(transaction)) {
                TransactionResult transactionResult = transactionService.evaluateTransaction(transaction);
                transaction.setResult(transactionResult.getResult());
                transaction.setInfo(transactionResult.getInfo());

                transactionRepository.save(transaction);

                logger.debug("PostMapping /api/antifraud/transaction2 SAVED: " + transaction.toDebugString());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(transactionResult);
            } else {
                logger.debug("PostMapping /api/antifraud/transaction3 " + "HttpStatus.BAD_REQUEST");
                return ResponseEntity.status(400).body("Bad Request");
            }
        }
        logger.debug("PostMapping /api/antifraud/transaction4 " + checkUser.getUsername()
                + " role: " + checkUser.getRole() + " Forbidden");
        return ResponseEntity.status(HttpStatus.valueOf(403)).body("Forbidden");

    }

    @PostMapping(value = "/api/auth/user", produces="application/json")
    public ResponseEntity processUser ( @RequestBody UserParameters userParameters) {

        if (userParameters.getUsername() == null || userParameters.getName() == null
                || userParameters.getPassword() == null) {
            logger.debug("PostMapping/api/auth/user " + "HttpStatus.BAD_REQUEST");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("    ");
        }

        UserParameters userParam = userParametersService.findByUsername(userParameters.getUsername());
        if (userParam != null) {
            logger.debug("/api/auth/user" + "HttpStatus.CONFLICT");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("    ");
        }
        userParameters = userParametersService.saveFirstTime(userParameters);//first saving of password needs processing

        UserParameters userParam2 = userParametersService.save(userParameters);
        logger.debug("/api/auth/user" + userParam2.getName());
        return ResponseEntity.status(HttpStatus.valueOf(201)).body(userParam2.new UserViewerId());
    }

    @PutMapping(value = "/api/auth/access", produces="application/json")
    public ResponseEntity accessControl ( @RequestBody UserStatus userStatus) {
        UserParameters checkAdmin = userParametersService
                .findByUsername(currentUserName());

        if (checkAdmin.getRole().equals(SecurityParams.ADMINISTRATOR)) {
            logger.debug("/api/auth/access1 Operation: " + userStatus.getUsername()
                    + " -> " + userStatus.getOperation());
            String lockState = "NONE";

            UserParameters user = userParametersService.findByUsername(userStatus.getUsername());
            if (user == null) {
                logger.debug("/api/auth/access2 " + userStatus.getUsername());
                return ResponseEntity.status(HttpStatus.valueOf(404)).body(userStatus.getUsername()
                        + " not found");
            }
            if (userStatus.getOperation().equals(SecurityParams.OP_LOCK)) {
                if (user.getRole().equals(SecurityParams.ADMINISTRATOR)) {
                    logger.debug("/api/auth/access3 " + userStatus.getUsername()
                            + ": " + user.getRole() + " -> " + userStatus.getOperation());
                    return ResponseEntity.status(HttpStatus.valueOf(400)).body("Bad request "
                            + userStatus.getOperation());
                }
                lockState = SecurityParams.LOCKED;
            } else if (userStatus.getOperation().equals(SecurityParams.OP_UNLOCK)) {
                lockState = SecurityParams.UNLOCKED;
            } else {
                logger.debug("/api/auth/access4 " + userStatus.getUsername()
                        + " -> " + userStatus.getOperation());
                return ResponseEntity.status(HttpStatus.valueOf(400)).body("Bad request "
                        + userStatus.getOperation());
            }

            user.setStatus(lockState);
            userParametersService.save(user);
            logger.debug("/api/auth/access5 " + user.getName() + " -> " + lockState);
            return ResponseEntity.status(HttpStatus.valueOf(200)).body("{\n" +
                    "    \"status\": \"User " + user.getUsername() + " " + lockState + "!\"\n" +
                    "}");
        }
        logger.debug("/api/auth/access6 " + userStatus.getUsername() + " -> " + userStatus.getOperation() + " Forbidden");
        return ResponseEntity.status(HttpStatus.valueOf(403)).body("Forbidden");
    }

    @PutMapping(value = "/api/auth/role", produces="application/json")
    public ResponseEntity roleControl ( @RequestBody UserParameters userParameters) {
        UserParameters checkAdmin = userParametersService
                .findByUsername(currentUserName());
        if (checkAdmin.getRole().equals(SecurityParams.ADMINISTRATOR)) {

            UserParameters user = userParametersService.findByUsername( userParameters.getUsername());
            if (user == null) {
                logger.debug("/api/auth/role1 " + userParameters.getUsername());
                return ResponseEntity.status(HttpStatus.valueOf(404)).body(userParameters.getUsername()
                        + " not found");
            }
            if (user.getRole().equals(SecurityParams.ADMINISTRATOR)) {
                logger.debug("/api/auth/role2 " + userParameters.getUsername() + " -> " + userParameters.getRole());
                return ResponseEntity.status(HttpStatus.valueOf(400)).body("Bad request ADMINISTRATOR -> "
                        + userParameters.getRole());
            }
            if (userParameters.getRole().equals(SecurityParams.ADMINISTRATOR)
                    || !SecurityParams.roleList.contains(userParameters.getRole())) {
                logger.debug("/api/auth/role3 " + userParameters.getUsername() + " -> " + userParameters.getRole());
                return ResponseEntity.status(HttpStatus.valueOf(400)).body("Bad request  -> "
                        + userParameters.getRole());
            }
            if (user.getRole().equals(userParameters.getRole())) {
                logger.debug("/api/auth/role4 " + userParameters.getUsername() + " -> " + userParameters.getRole());
                return ResponseEntity.status(HttpStatus.valueOf(409)).body("Conflict -> "
                        + userParameters.getRole());
            }


            user.setRole(userParameters.getRole());
            userParametersService.save(user);
            logger.debug("/api/auth/role4 " + user.getName() + " -> " + user.getRole());
            return ResponseEntity.status(HttpStatus.valueOf(200)).body(user.new UserViewerId());
        }
        logger.debug("/api/auth/role5 " + userParameters.getUsername() + " -> " + userParameters.getRole() + " Forbidden");
        return ResponseEntity.status(HttpStatus.valueOf(403)).body("Forbidden");
    }
    @GetMapping (value = "/api/auth/list", produces="application/json")
    public ResponseEntity listUsers (String userName){
        UserParameters checkAdmin = userParametersService
                .findByUsername(currentUserName());
        if ((checkAdmin.getRole().equals(SecurityParams.ADMINISTRATOR)) ||
                    checkAdmin.getRole().equals(SecurityParams.SUPPORT)) {
            List<UserParameters> userParametersList = userParametersService.findAll();
            logger.debug("GetMapping /api/auth/list1 "
                    + userParametersList.stream().map(u -> "\n" + u.getUsername()).toList());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(userParametersList.stream().map(u -> u.new UserViewerId()).toList());
        }

        logger.debug("GetMapping /api/auth/list2 " + checkAdmin.getUsername() + " role: " + checkAdmin.getRole());
        return ResponseEntity.status(HttpStatus.valueOf(403)).body("Bad request "
                + checkAdmin.getRole());
    }


    @DeleteMapping (value = "/api/auth/user/{username}",  produces="application/json")
    public ResponseEntity deleteUser ( @PathVariable String username) {
        logger.debug("DeleteMapping /api/auth/user/{username}1 " + username);
        return processDeleteRequest(username);
    }

    private ResponseEntity<?> processDeleteRequest(String username) {
        UserParameters checkAdmin = userParametersService
                    .findByUsername(currentUserName());

        if (checkAdmin.getRole().equals(SecurityParams.ADMINISTRATOR) && username != null) {
            String result = userParametersService.delete(username);
            logger.debug("DeleteMapping processDeleteRequest1 " + username + " Result: " + result);
            if (result.equals("0")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.OK).body("{\n" +
                    "   \"username\": " + username + ",\n" +
                    "   \"status\": \"Deleted successfully!\"\n" +
                    "}");
        }
        logger.debug("DeleteMapping op for " + checkAdmin.getUsername()
                + " role: " + checkAdmin.getRole() + " Forbidden");
        return ResponseEntity.status(HttpStatus.valueOf(403)).body("Forbidden");
    }

    @DeleteMapping (value = "/api/auth/user",  produces="application/json")
    public ResponseEntity deleteWithRequest ( @RequestParam String username) {
        logger.debug("DeleteMapping /api/auth/user1 " + username);
        return processDeleteRequest(username);
    }
        
    //POST, DELETE, GET api/antifraud/suspicious-ip SUPPORT role only
    @PostMapping (value = "api/antifraud/suspicious-ip", produces="application/json")
    public ResponseEntity addToIpList(@RequestBody SuspiciousIp ipToSave) {
        UserParameters currentUser = userParametersService
                .findByUsername(currentUserName());

        if (!currentUser.getRole().equals(SecurityParams.SUPPORT)) {
            logger.debug("PostMapping /api/antifraud/suspicious-ip1 " + currentUser.getUsername()
                    + " role: " + currentUser.getRole() + " Forbidden");
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        if (currentUser.getStatus().equals(SecurityParams.LOCKED)) {
            logger.debug("PostMapping /api/antifraud/suspicious-ip2 "
                    + currentUser.getUsername() + " " + currentUser.getStatus());
            return ResponseEntity.status(401).body("User status LOCKED");
        }
        if (!ParameterChecker.isValidIPAddress(ipToSave.getIp())) {
            logger.debug("PostMapping /api/antifraud/suspicious-ip3 "
                    + currentUser.getUsername() + " posted bad IP: " + ipToSave.getIp());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Bad Request: " + ipToSave.getIp());
        }
        if (suspiciousIpService.findByIp(ipToSave.getIp()) != null){
            logger.debug("PostMapping /api/antifraud/suspicious-ip4 "
                    + currentUser.getUsername() + " posted conflict: " + ipToSave.getIp());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: " + ipToSave.getIp());
        }
        SuspiciousIp ip = suspiciousIpService.save(ipToSave);
        logger.debug("PostMapping /api/antifraud/suspicious-ip5 "
                + currentUser.getUsername() + " saved " + ip.getIp());
        return ResponseEntity.status(HttpStatus.OK).body(ip);
    }

    @GetMapping (value = "api/antifraud/suspicious-ip", produces="application/json")
    public ResponseEntity receiveIpList() {
        UserParameters currentUser = userParametersService
                .findByUsername(currentUserName());
        if (!currentUser.getRole().equals(SecurityParams.SUPPORT)) {
            logger.debug("GetMapping /api/antifraud/suspicious-ip1 " + currentUser.getUsername()
                    + " role: " + currentUser.getRole() + " Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        if (currentUser.getStatus().equals(SecurityParams.LOCKED)) {
            logger.debug("GetMapping /api/antifraud/suspicious-ip2 "
                    + currentUser.getUsername() + " " + currentUser.getStatus());
            return ResponseEntity.status(401).body("User status LOCKED");
        }

        List<SuspiciousIp> ipList = suspiciousIpService.findAll();
        logger.debug("GetMapping /api/antifraud/suspicious-ip3 "
                + ipList.stream().map(u -> "\n" + u.getIp()).toList());
        return ResponseEntity.status(HttpStatus.OK).body(ipList);
    }

    @DeleteMapping (value = "api/antifraud/suspicious-ip/{ipToRemove}", produces="application/json")
    public ResponseEntity removeIp (@PathVariable String ipToRemove) {
        UserParameters currentUser = userParametersService
                .findByUsername(currentUserName());
        if (!currentUser.getRole().equals(SecurityParams.SUPPORT)) {
            logger.debug("DeleteMapping api/antifraud/suspicious-/{ipToRemove}1 " + currentUser.getUsername()
                    + " role: " + currentUser.getRole() + " Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        if (currentUser.getStatus().equals(SecurityParams.LOCKED)) {
            logger.debug("DeleteMapping /api/antifraud/suspicious-ip/{ipToRemove}2 "
                    + currentUser.getUsername() + " " + currentUser.getStatus());
            return ResponseEntity.status(401).body("User status LOCKED");
        }
        if (!ParameterChecker.isValidIPAddress(ipToRemove)) {
            logger.debug("DeleteMapping /api/antifraud/suspicious-ip/{ipToRemove}3 "
                    + currentUser.getUsername() + " posted bad IP: " + ipToRemove);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Bad Request: " + ipToRemove);
        }
        SuspiciousIp suspiciousIp = suspiciousIpService.findByIp(ipToRemove);
        if ( suspiciousIp == null){
            logger.debug("DeleteMapping /api/antifraud/suspicious-ip/{ipToRemove}4 "
                    + currentUser.getUsername() + " Not Found: " + ipToRemove);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found: " + ipToRemove);
        }
        suspiciousIpService.delete(suspiciousIp);
        logger.debug("DeleteMapping /api/antifraud/suspicious-ip/{ipToRemove}5 "
                + currentUser.getUsername() + " deleted " + suspiciousIp.getIp());
        return ResponseEntity.status(HttpStatus.OK).body("{\n" +
                "   \"status\": \"IP " + suspiciousIp.getIp() + " successfully removed!\"\n" +
                "}");
    }


    //POST, DELETE, GET api/antifraud/stolencard SUPPORT role only

    @PostMapping (value = "api/antifraud/stolencard", produces="application/json")
    public ResponseEntity addToStolenCardList(@RequestBody StolenCard cardToSave) {
        UserParameters currentUser = userParametersService
                .findByUsername(currentUserName());
        if (!currentUser.getRole().equals(SecurityParams.SUPPORT)) {
            logger.debug("PostMapping /api/antifraud/stolencard1 " + currentUser.getUsername()
                    + " role: " + currentUser.getRole() + " Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        if (currentUser.getStatus().equals(SecurityParams.LOCKED)) {
            logger.debug("PostMapping /api/antifraud/stolencard2 "
                    + currentUser.getUsername() + " " + currentUser.getStatus());
            return ResponseEntity.status(401).body("User status LOCKED");
        }
        if (!ParameterChecker.checkLuhn(cardToSave.getNumber())) {
            logger.debug("PostMapping /api/antifraud/stolencard3 "
                    + currentUser.getUsername() + " posted bad card number: "
                    + cardToSave.getNumber());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Bad Request: " + cardToSave.getNumber());
        }
        StolenCard stolenCard = stolenCardService.findByNumber(cardToSave.getNumber());
        if (stolenCard != null){
            logger.debug("PostMapping /api/antifraud/stolencard " + currentUser.getUsername()
                    + " posted conflict: " + stolenCard.getNumber());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Conflict: " + stolenCard.getNumber());
        }
        StolenCard stolenCard1 = stolenCardService.save(cardToSave);
        logger.debug("PostMapping /api/antifraud/suspicious-ip5 "
                + currentUser.getUsername() + " saved " + stolenCard1.getNumber());
        return ResponseEntity.status(HttpStatus.OK).body(stolenCard1);
    }

    @GetMapping (value = "api/antifraud/stolencard", produces="application/json")
    public ResponseEntity receiveStolenCardList() {
        UserParameters currentUser = userParametersService
                .findByUsername(currentUserName());
        if (!currentUser.getRole().equals(SecurityParams.SUPPORT)) {
            logger.debug("GetMapping /api/antifraud/stolencard1 " + currentUser.getUsername()
                    + " role: " + currentUser.getRole() + " Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        if (currentUser.getStatus().equals(SecurityParams.LOCKED)) {
            logger.debug("GetMapping /api/antifraud/stolencard2 "
                    + currentUser.getUsername() + " " + currentUser.getStatus());
            return ResponseEntity.status(401).body("User status LOCKED");
        }

        List<StolenCard> stolenCardList = stolenCardService.findAll();
        logger.debug("GetMapping /api/antifraud/stolencard3 "
                + stolenCardList.stream().map(u -> "\n" + u.getNumber()).toList());
        return ResponseEntity.status(HttpStatus.OK).body(stolenCardList);
    }

    @DeleteMapping (value = "api/antifraud/stolencard/{cardNoToRemove}", produces="application/json")
    public ResponseEntity removeCardNo (@PathVariable String cardNoToRemove) {
        UserParameters currentUser = userParametersService
                .findByUsername(currentUserName());
        if (!currentUser.getRole().equals(SecurityParams.SUPPORT)) {
            logger.debug("DeleteMapping api/antifraud/suspicious-/{cardNoToRemove}1 "
                    + currentUser.getUsername()
                    + " role: " + currentUser.getRole() + " Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        if (currentUser.getStatus().equals(SecurityParams.LOCKED)) {
            logger.debug("DeleteMapping /api/antifraud/suspicious-ip/{cardNoToRemove}2 "
                    + currentUser.getUsername() + " " + currentUser.getStatus());
            return ResponseEntity.status(401).body("User status LOCKED");
        }
        if (!ParameterChecker.checkLuhn(cardNoToRemove)) {
            logger.debug("DeleteMapping /api/antifraud/stolencard/{cardNoToRemove}3 "
                    + currentUser.getUsername() + " posted bad cardNo: " + cardNoToRemove);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Bad Request: " + cardNoToRemove);
        }
        StolenCard stolenCard = stolenCardService.findByNumber(cardNoToRemove);
        if ( stolenCard == null){
            logger.debug("DeleteMapping /api/antifraud/stolencard/{cardNoToRemove}4 "
                    + currentUser.getUsername() + " Not Found: " + cardNoToRemove);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found: "
                    + cardNoToRemove);
        }
        stolenCardService.delete(stolenCard);
        logger.debug("DeleteMapping /api/antifraud/suspicious-ip/{ipToRemove}5 "
                + currentUser.getUsername() + " deleted " + stolenCard.getNumber());
        return ResponseEntity.status(HttpStatus.OK).body("{\n" +
                "   \"status\": \"Card " + stolenCard.getNumber() + " successfully removed!\"\n" +
                "}");
    }

    //PUT /api/antifraud/transaction
    @PutMapping(value = "/api/antifraud/transaction", produces="application/json")
    public ResponseEntity transactionFeedback( @RequestBody TransactionFeedback transactionFeedback) {
        UserParameters currentUser = userParametersService
                .findByUsername(currentUserName());
        if (!currentUser.getRole().equals(SecurityParams.SUPPORT)) {
            logger.debug("PutMapping /api/antifraud/transaction1 "
                    + currentUser.getUsername()
                    + " role: " + currentUser.getRole() + " Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        if (currentUser.getStatus().equals(SecurityParams.LOCKED)) {
            logger.debug("PutMapping /api/antifraud/transaction2 "
                    + currentUser.getUsername() + " " + currentUser.getStatus());
            return ResponseEntity.status(401).body("User status LOCKED");
        }
        Transaction transaction = transactionRepository
                .findById(transactionFeedback.getTransactionId());
        if (transaction == null) {
            logger.debug( "PutMapping /api/antifraud/transaction3 id not found: "
                    + transactionFeedback.getTransactionId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
        }
        //validate feedback!
        if (!TransactionLimitsService.TRANSACTION_RESULT_LIST
                .contains(transactionFeedback.getFeedback())) {
            logger.debug("PutMapping /api/antifraud/transaction5 " + "Bad Request "
                    + transactionFeedback.getFeedback());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request");
        }

        if (transaction.getFeedback() != null) {
            logger.debug( "PutMapping /api/antifraud/transaction4 conflict: "
                    + transactionFeedback.getFeedback() + " " + transaction.toDebugString());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict");
        }
        //check for exception in table
        if ( transaction.getResult().equals(transactionFeedback.getFeedback()) ) {
               // || (transaction.getInfo() != null && !transaction.getInfo().equals(TransactionService.REJECT_REASON_NONE))) {
            logger.debug("PutMapping /api/antifraud/transaction6 " + "Unprocessable Entity "
                    + transactionFeedback.getFeedback());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Unprocessable Entity");
        }

        transaction.setFeedback(transactionFeedback.getFeedback());
        transactionService.save(transaction);

        transactionLimitsService
            .processFeedback(transaction, transactionFeedback);
        logger.debug("PutMapping /api/antifraud/transaction7 "
                + transactionFeedback.getFeedback() + " " + transaction.toDebugString());
        return ResponseEntity.status(HttpStatus.OK).body(transaction.new TransactionView());

    }

    //GET /api/antifraud/history
    @GetMapping(value = "/api/antifraud/history", produces="application/json")
    public ResponseEntity transactionList( ) {
        UserParameters currentUser = userParametersService
                .findByUsername(currentUserName());
        if (!currentUser.getRole().equals(SecurityParams.SUPPORT)) {
            logger.debug("GetMapping /api/antifraud/history1 "
                    + currentUser.getUsername()
                    + " role: " + currentUser.getRole() + " Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        if (currentUser.getStatus().equals(SecurityParams.LOCKED)) {
            logger.debug("GetMapping /api/antifraud/history2 "
                    + currentUser.getUsername() + " " + currentUser.getStatus());
            return ResponseEntity.status(401).body("User status LOCKED");
        }
        List<Transaction> transactionListAll = transactionService.findAll();
        logger.debug("/api/antifraud/history3 " + transactionListAll.stream()
                .map(u -> "\n" + u.toDebugString()).collect(Collectors.toList()));
        return ResponseEntity.status(HttpStatus.OK).body(transactionListAll.stream().map(u -> u.new TransactionView())
                );

    }

    //GET /api/antifraud/history/{number}
    @GetMapping(value = "/api/antifraud/history/{number}", produces="application/json")
    public ResponseEntity transactionListWithNumber( @PathVariable String number ) {
        UserParameters currentUser = userParametersService
                .findByUsername(currentUserName());
        if (!currentUser.getRole().equals(SecurityParams.SUPPORT)) {
            logger.debug("GetMapping /api/antifraud/history/{number}1 "
                    + currentUser.getUsername()
                    + " role: " + currentUser.getRole() + " Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        if (currentUser.getStatus().equals(SecurityParams.LOCKED)) {
            logger.debug("GetMapping /api/antifraud/history/{number}2 "
                    + currentUser.getUsername() + " " + currentUser.getStatus());
            return ResponseEntity.status(401).body("User status LOCKED");
        }
        if (!ParameterChecker.checkLuhn(number)) {
            logger.debug("DeleteMapping /api/antifraud/stolencard/{number}3 "
                    + currentUser.getUsername() + " posted bad cardNo: " + number);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Bad Request: " + number);
        }

        List<Transaction> transactionListAll =
                transactionService.findAllByNumber(number);

        if(transactionListAll.size() == 0) {
            logger.debug("/api/antifraud/history/{number}4 " + number + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
        logger.debug("/api/antifraud/history/{number}5 " + transactionListAll.stream()
                .map(u -> "\n" + u.toDebugString()).collect(Collectors.toList()));
        return ResponseEntity.status(HttpStatus.OK).body(transactionListAll.stream()
                .map(u -> u.new TransactionView()));


    }
}
