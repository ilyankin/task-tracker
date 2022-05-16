package exceptions;

public class ManagerIllegalMethodRequestException extends RuntimeException {
    public ManagerIllegalMethodRequestException(String path, String method, String... supportedMethod) {
        super("The " + path + " context doesn't support " + method + " method. "
                + "Supported methods: " + String.join(", ", supportedMethod));
    }
}
