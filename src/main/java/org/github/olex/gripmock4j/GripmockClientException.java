package org.github.olex.gripmock4j;

public class GripmockClientException extends RuntimeException {

    public GripmockClientException(String message) {
        super(message);
    }

    public GripmockClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
