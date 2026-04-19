package mk.ukim.finki.etnomk.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class JwtExceptionHandler {

    private ResponseEntity<JwtExceptionResponse> buildJwtExceptionResponse(
            HttpStatus status,
            String message,
            String path
    ) {
        return new ResponseEntity<>(
                new JwtExceptionResponse(status.value(), status.getReasonPhrase(), message, path),
                status
        );
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<JwtExceptionResponse> handleExpiredJwtException(
            ExpiredJwtException ex, HttpServletRequest request) {
        return buildJwtExceptionResponse(HttpStatus.UNAUTHORIZED, "The token has expired.", request.getRequestURI());
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<JwtExceptionResponse> handleSignatureException(
            SignatureException ex, HttpServletRequest request) {
        return buildJwtExceptionResponse(HttpStatus.UNAUTHORIZED, "The token's signature is invalid.", request.getRequestURI());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<JwtExceptionResponse> handleJwtException(
            JwtException ex, HttpServletRequest request) {
        return buildJwtExceptionResponse(HttpStatus.UNAUTHORIZED, "The token is invalid.", request.getRequestURI());
    }

    public static class JwtExceptionResponse {

        private final int status;
        private final String error;
        private final String message;
        private final String path;

        public JwtExceptionResponse(int status, String error, String message, String path) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }
    }
}
