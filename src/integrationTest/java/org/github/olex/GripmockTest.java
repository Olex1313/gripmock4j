package org.github.olex;

import io.grpc.ManagedChannelBuilder;
import org.github.olex.grpc.GripmockGrpc;
import org.github.olex.grpc.Simple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
class GripmockTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Container
    private static final GripmockContainer gripmockContainer = new GripmockContainer("/proto/simple.proto")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(GripmockContainer.class)))
            .withFileSystemBind("src/integrationTest/resources/proto", "/proto");

    @AfterEach
    void tearDown() {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(gripmockContainer.getStubAdminAddress() + "/clear"))
                .build();
        try {
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getStubMappings() {
        // given
        Gripmock gripmock = new Gripmock(gripmockContainer.getHost(), gripmockContainer.getHttpServerPort());
        var channelBuilder = ManagedChannelBuilder.forTarget(gripmockContainer.getGrpcServerAddress()).usePlaintext();
        GripmockGrpc.GripmockBlockingStub gripmockBlockingStub = GripmockGrpc.newBlockingStub(channelBuilder.build());
        var request = Simple.Request.newBuilder()
                .setName("olex")
                .build();

        // when
        gripmock.addStubMapping(
                new Stubbing(
                        GripmockGrpc.SERVICE_NAME.split("\\.")[1],
                        GripmockGrpc.getSayHelloMethod().getBareMethodName(),
                        new Equal(Map.of("name", request.getName())),
                        new OutputData(Map.of("message", "hi!", "return_code", 201))
                )
        );

        // then
        var response = gripmockBlockingStub.sayHello(request);
        assertThat(response.getMessage()).isEqualTo("hi!");
        assertThat(response.getReturnCode()).isEqualTo(201);
    }

    @Test
    public void clear() {
        // given
        Gripmock gripmock = new Gripmock(gripmockContainer.getHost(), gripmockContainer.getHttpServerPort());
        createDummyStubbing();

        // when
        gripmock.clear();

        // then
        assertNoDummyStubbingExists();
    }

    @Test
    void addStubMapping() {
        Gripmock gripmock = new Gripmock(gripmockContainer.getHost(), gripmockContainer.getHttpServerPort());
        var channelBuilder = ManagedChannelBuilder.forTarget(gripmockContainer.getGrpcServerAddress())
                .usePlaintext();
        GripmockGrpc.GripmockBlockingStub gripmockBlockingStub = GripmockGrpc.newBlockingStub(channelBuilder.build());
        var request = Simple.Request.newBuilder()
                .setName("olex")
                .build();

        // when
        gripmock.addStubMapping(
                new Stubbing(
                        GripmockGrpc.SERVICE_NAME.split("\\.")[1],
                        GripmockGrpc.getSayHelloMethod().getBareMethodName(),
                        new Equal(
                                Map.of("name", request.getName())
                        ),
                        new OutputData(
                                Map.of("message", "hi!", "return_code", 201)
                        )
                )
        );

        // then
        var response = gripmockBlockingStub.sayHello(request);
        assertThat(response.getMessage()).isEqualTo("hi!");
        assertThat(response.getReturnCode()).isEqualTo(201);
    }

    private void assertNoDummyStubbingExists() {
        try {
            var request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("""
                            {
                            	"service":"Gripmock",
                             	"method":"sayHello",
                            	"data": {
                            		"name":"olex"
                            	}
                            }
                            """))
                    .uri(URI.create(gripmockContainer.getStubAdminAddress() + "/clear")).build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            assert response.statusCode() / 100 != 2;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createDummyStubbing() {
        try {
            var request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("""
                            {
                                "service":"Gripmock",
                                "method":"sayHello",
                                "input": {
                                    "equals": {
                                        "name":"olex"
                                    }
                                },
                                "output": {
                                    "data": {
                                        "return_code":201,
                                        "message":"hi!"
                                    }
                                }
                            }
                            """))
                    .uri(URI.create(gripmockContainer.getStubAdminAddress() + "/add")).build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            assertThat(response.statusCode() / 100).isEqualTo(2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}