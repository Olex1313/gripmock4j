package org.github.olex;

public record Stubbing(
        String service, String method, Equal input, OutputData output
) {
}
