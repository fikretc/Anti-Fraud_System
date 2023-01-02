package antifraud;

import antifraud.business.Amount;
import antifraud.business.UserParameters;
import antifraud.business.UserParametersService;
import antifraud.business.UserStatus;
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

@Controller
public class AntiFraudController {

    @Autowired
    UserParametersService userParametersService;

    private static final Logger logger = LogManager.getLogger(AntiFraudController.class);

    @Autowired
    private IAuthenticationFacade authenticationFacade;


    public String currentUserName() {
        final Authentication authentication = authenticationFacade.getAuthentication();
        return authentication.getName();
    }


    @PostMapping(value="/api/antifraud/transaction", produces="application/json")
    public ResponseEntity processAmount(@RequestBody Amount amount) {
        UserParameters checkUser = userParametersService
                .findByUsername(currentUserName());

        if (checkUser.getRole().equals(SecurityParams.MERCHANT)) {

            if (checkUser.getStatus().equals(SecurityParams.LOCKED)) {
                logger.debug("PostMapping/api/antifraud/transaction "
                        + checkUser.getUsername() + " " + checkUser.getStatus());
                return ResponseEntity.status(401).body("User status LOCKED");
            }
            if (amount.validate()) {
                logger.debug("PostMapping/api/antifraud/transaction " + amount.processingType());
                return ResponseEntity.status(HttpStatus.OK)
                        .body("{\n    result : \"" + amount.processingType() + "\"\n}");
            } else {
                logger.debug("PostMapping/api/antifraud/transaction " + "HttpStatus.BAD_REQUEST");
                return ResponseEntity.status(400).body("Bad Request");
            }
        }
        logger.debug("/api/antifraud/transaction " + checkUser.getUsername() + " role: " + checkUser.getRole() + " Forbidden");
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
            logger.debug("GetMapping/api/auth/list1 "
                    + userParametersList.stream().map(u -> "\n" + u.getName()).toList());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(userParametersList.stream().map(u -> u.new UserViewerId()).toList());
        }

        logger.debug("/api/auth/list2 " + checkAdmin.getUsername() + " role: " + checkAdmin.getRole());
        return ResponseEntity.status(HttpStatus.valueOf(403)).body("Bad request "
                + checkAdmin.getRole());
    }


    @DeleteMapping (value = "/api/auth/user/{username}",  produces="application/json")
    public ResponseEntity deleteUser ( @PathVariable String username) {
        logger.debug("DeleteMapping /api/auth/user/{username} " + username);
        return processDeleteRequest(username);
    }

    private ResponseEntity<?> processDeleteRequest(String username) {
        UserParameters checkAdmin = userParametersService
                    .findByUsername(currentUserName());

        if (checkAdmin.getRole().equals(SecurityParams.ADMINISTRATOR) && username != null) {
            String result = userParametersService.delete(username);
            logger.debug("DeleteMapping " + username + " Result: " + result);
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
        logger.debug("DeleteMapping /api/auth/user " + username);
        return processDeleteRequest(username);
    }


    }
