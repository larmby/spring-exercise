package uk.co.pepper.companysearch.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<Object> handleCompanyNotFoundException(final CompanyNotFoundException companyNotFoundException,
                                                                 final WebRequest webRequest) {
        return handleException(companyNotFoundException, HttpStatus.NOT_FOUND, webRequest);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(final BadRequestException badRequestException,
                                                                 final WebRequest webRequest) {
        return handleException(badRequestException, HttpStatus.BAD_REQUEST, webRequest);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Object> handleServiceUnavailableException(final ServiceUnavailableException serviceUnavailableException,
                                                                 final WebRequest webRequest) {
        return handleException(serviceUnavailableException, HttpStatus.SERVICE_UNAVAILABLE, webRequest);
    }

    @ExceptionHandler(TruApiAuthException.class)
    public ResponseEntity<Object> handleTruNarrativeAuthException(final TruApiAuthException truApiAuthException,
                                                                 final WebRequest webRequest) {
        return handleException(truApiAuthException, HttpStatus.FORBIDDEN, webRequest);
    }

    private ResponseEntity<Object> handleException(final Exception exception,
                                                   final HttpStatus httpStatus,
                                                   final WebRequest webRequest) {
        return handleExceptionInternal(exception, exception.getMessage(), new HttpHeaders(), httpStatus, webRequest);
    }
}
