package com.openclassrooms.etudiant.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

/**
 * Translates controller exceptions into HTTP responses and logs errors.
 */
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles an invalid argument or application state.
     *
     * @param runtimeException the exception to translate
     * @param request the affected request
     * @return an HTTP 400 response containing the error details
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException runtimeException, WebRequest request) {
        logError(runtimeException);
        return handleExceptionInternal(runtimeException, getErrorDetails(runtimeException, request), new HttpHeaders(),
                HttpStatus.BAD_REQUEST, request);
    }


    /**
     * Handles an authentication exception caused by incorrect credentials.
     *
     * @param badCredentialsException the authentication exception
     * @param request the affected request
     * @return an HTTP 401 response containing the error details
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = {BadCredentialsException.class})
    protected ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException badCredentialsException,
                                                                   WebRequest request) {
        logError(badCredentialsException);
        return handleExceptionInternal(badCredentialsException, getErrorDetails(badCredentialsException, request),
                new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    /**
     * Handles an access denial represented by {@link AccessDeniedException}.
     *
     * @param accessDeniedException the access denial exception
     * @param request the affected request
     * @return an HTTP 403 response containing the error details
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = {AccessDeniedException.class})
    protected ResponseEntity<Object> handleForbiddenException(AccessDeniedException accessDeniedException,
                                                              WebRequest request) {
        logError(accessDeniedException);
        return handleExceptionInternal(accessDeniedException, getErrorDetails(accessDeniedException, request),
                new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }


    /**
     * Produces a generic response for an internal error handled by this method.
     *
     * @param runtimeException the exception to log
     * @param request the affected request
     * @return an HTTP 500 response containing a generic message
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleException(RuntimeException runtimeException, WebRequest request) {
        logError(runtimeException);
        return handleExceptionInternal(runtimeException, "Internal Server error", new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Logs the exception message and stack trace.
     *
     * @param exception the exception to log
     */
    private void logError(Exception exception) {
        logger.error(exception.getMessage(), exception);
    }

    /**
     * Builds error details from an exception and its request.
     *
     * @param exception the exception to describe
     * @param request the affected request
     * @return the timestamped error details
     */
    private ErrorDetails getErrorDetails(Exception exception, WebRequest request) {
        return new ErrorDetails(LocalDateTime.now(), exception.getMessage(), request.getDescription(false));
    }
}
