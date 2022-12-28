package antifraud;

import antifraud.business.Amount;
import antifraud.business.UserParameters;
import antifraud.business.UserParametersService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AntiFraudController {

    @Autowired
    UserParametersService userParametersService;
    private static final Logger logger = LogManager.getLogger(AntiFraudController.class);

    @PostMapping(value="/api/antifraud/transaction", produces="application/json")
    public ResponseEntity processAmount(@RequestBody Amount amount) {
        if (amount.validate()) {
            logger.debug("PostMapping/api/antifraud/transaction " + amount.processingType());
            return ResponseEntity.status(HttpStatus.OK)
                    .body("{\n    result : \"" + amount.processingType() + "\"\n}");
        }
        else {
            logger.debug("PostMapping/api/antifraud/transaction " + "HttpStatus.BAD_REQUEST");
            return ResponseEntity.status(400).body("Bad Request");
        }
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

        UserParameters userParam2 = userParametersService.save(userParameters);
        logger.debug("/api/auth/user" + userParam2.getName());
        return ResponseEntity.status(HttpStatus.valueOf(201)).body(userParam2.new UserViewerId());
    }

    @GetMapping (value = "/api/auth/list", produces="application/json")
    public ResponseEntity listUsers (String userName){
        List<UserParameters> userParametersList = userParametersService.findAll();
        logger.debug( "GetMapping/api/auth/list " + userParametersList.stream().map(u -> "\n" + u.getName()).toList());
        return ResponseEntity.status(HttpStatus.OK).body(userParametersList.stream().map(u -> u.new UserViewerId()).toList());
    }

    @DeleteMapping (value = "/api/auth/user/{username}",  produces="application/json")
    public ResponseEntity deleteUser ( @PathVariable String username) {
        String result = userParametersService.delete(username);
        logger.debug("DeleteMapping/api/auth/user/{username} " + username + " Result: " + result);
        if (result.equals("0")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body("{\n" +
                "   \"username\": " + username + ",\n" +
                "   \"status\": \"Deleted successfully!\"\n" +
                "}");

    }


}
