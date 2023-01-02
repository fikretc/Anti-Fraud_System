package antifraud.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter
            (final MissingServletRequestParameterException ex, final HttpHeaders headers,
             final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        logger.debug("ResponseEntityExceptionHandler " + request.getContextPath()
                + " Forbidden");
        return ResponseEntity.status(HttpStatus.valueOf(403)).body("Forbidden");
    }

}
