package org.github.olex.gripmock4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.olex.gripmock4j.stub.Stub;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Gripmock {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient;
    private final String address;

    public Gripmock(String address) {
        this(address, HttpClient.newHttpClient());
    }

    public Gripmock(String host, int port) {
        this("http://" + host + ":" + port, HttpClient.newHttpClient());
    }

    public Gripmock(String address, HttpClient httpClient) {
        this.address = address;
        this.httpClient = httpClient;
    }

    public void addStubMapping(Stub stubbing) {
        try {
            String body = objectMapper.writeValueAsString(stubbing);
            var request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .uri(URI.create(address + "/add"))
                    .build();
            doRequest(request, HttpResponse.BodyHandlers.ofString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(address + "/clear"))
                .build();
        doRequest(request, HttpResponse.BodyHandlers.ofString());
    }

    private <T> void doRequest(HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler) {
        try {
            HttpResponse<T> response = httpClient.send(httpRequest, bodyHandler);
            if (response.statusCode() / 100 != 2) {
                throw new GripmockClientException("Request to gripmock server status code is not 2xx, but instead %d"
                        .formatted(response.statusCode())
                );
            }
            response.body();
        } catch (IOException | InterruptedException e) {
            throw new GripmockClientException("Unexpected exception while doing http request to gripmock", e);
        }
    }

}
