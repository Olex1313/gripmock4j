package org.github.olex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Gripmock {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String address;

    public Gripmock(String address) {
        this.address = address;
    }

    public Gripmock(String host, int port) {
        this.address = "http://" + host + ":" + port;
    }

    public void addStubMapping(Stubbing stubbing) {
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

    public String getStubMappings() {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(address + "/"))
                .build();
        return doRequest(request, HttpResponse.BodyHandlers.ofString());
    }

    public void clear() {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(address + "/clear"))
                .build();
        doRequest(request, HttpResponse.BodyHandlers.ofString());
    }

    private <T> T doRequest(HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler) {
        try {
            HttpResponse<T> response = httpClient.send(httpRequest, bodyHandler);
            if (response.statusCode() / 100 != 2) {
                throw new GripmockClientException("Request to gripmock server status code is not 2xx, but instead %d"
                        .formatted(response.statusCode())
                );
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new GripmockClientException("Unexpected exception while doing http request to gripmock", e);
        }
    }

}
