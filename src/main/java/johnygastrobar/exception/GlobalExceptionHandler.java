package johnygastrobar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
// Para uma resposta de erro mais estruturada, podemos criar uma classe simples
// import java.time.LocalDateTime;
// import java.util.LinkedHashMap;
// import java.util.Map;

@ControllerAdvice // Esta anotação torna a classe um manipulador global de exceções
public class GlobalExceptionHandler {

    // Manipulador para ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        System.err.println("ResourceNotFoundException: " + ex.getMessage()); // Log do erro no servidor

        // Corpo da resposta de erro simples (apenas a mensagem da exceção)
        // String responseBody = ex.getMessage();
        // return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);

        // Ou um corpo de resposta JSON mais estruturado (exemplo comentado abaixo)
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getDescription(false) // URI da requisição
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Manipulador para ServiceException (erros de negócio ou acesso a dados não específicos)
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(
            ServiceException ex, WebRequest request) {

        System.err.println("ServiceException: " + ex.getMessage());
        if (ex.getCause() != null) {
            System.err.println("Causa: " + ex.getCause().getMessage());
        }

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(), // Ou INTERNAL_SERVER_ERROR dependendo da natureza
                ex.getMessage(),
                request.getDescription(false)
        );
        // Se a causa for SQLException, talvez um 500 seja mais apropriado
        // if (ex.getCause() instanceof java.sql.SQLException) {
        //     return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        // }
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }

    // Manipulador para IllegalArgumentException (ex: parâmetros de entrada inválidos)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        System.err.println("IllegalArgumentException: " + ex.getMessage());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Manipulador genérico para outras exceções não tratadas especificamente
    // É uma boa prática ter um fallback.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(
            Exception ex, WebRequest request) {

        System.err.println("Exception Genérica: " + ex.getMessage());
        ex.printStackTrace(); // Importante para depurar erros inesperados

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro inesperado no servidor.", // Mensagem genérica para o cliente
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // Classe auxiliar interna para uma resposta de erro JSON estruturada (opcional)
    // Se não usar esta classe, os handlers podem retornar apenas ex.getMessage() como corpo.
    private static class ApiErrorResponse {
        private int status;
        private String message;
        private String path;
        // private java.time.LocalDateTime timestamp; // Opcional

        public ApiErrorResponse(int status, String message, String path) {
            // this.timestamp = java.time.LocalDateTime.now();
            this.status = status;
            this.message = message;
            this.path = path;
        }

        // Getters para serialização JSON
        public int getStatus() { return status; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        // public java.time.LocalDateTime getTimestamp() { return timestamp; }
    }
}