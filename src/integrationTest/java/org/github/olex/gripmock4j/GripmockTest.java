package org.github.olex.gripmock4j;

import io.grpc.ManagedChannelBuilder;
import org.github.olex.grpc.GripmockGrpc;
import org.github.olex.grpc.Simple;
import org.github.olex.gripmock4j.stub.Stub;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
class GripmockTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Container
    private static final GripmockContainer gripmockContainer = new GripmockContainer(
            "/proto/simple.proto", "/proto/greeter.proto"
    ).withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(GripmockContainer.class)))
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
    public void clear() {
        // given
        Gripmock gripmock = new Gripmock(gripmockContainer.getStubAdminAddress());
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
        var req = Simple.Request.newBuilder()
                .setName("olex")
                .build();
        var res = Simple.Reply.newBuilder()
                .setMessage("hi!")
                .setReturnCode(201)
                .build();

        // when
        gripmock.addStubMapping(
                Stub.newStub()
                        .forService(GripmockGrpc.getServiceDescriptor())
                        .withMethod(GripmockGrpc.getSayHelloMethod())
                        .forCall(req)
                        .answer(res)
        );

        // then
        var response = gripmockBlockingStub.sayHello(req);
        assertThat(response.getMessage()).isEqualTo(res.getMessage());
        assertThat(response.getReturnCode()).isEqualTo(res.getReturnCode());
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
        } catch (IOException | InterruptedException e) {
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
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}