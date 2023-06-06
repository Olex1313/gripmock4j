package org.github.olex.gripmock4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.olex.grpc.GreeterGrpc;
import org.github.olex.grpc.GreeterOuterClass;
import org.github.olex.gripmock4j.stub.Stub;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class StubTest {

    @Test
    public void newStub() throws JsonProcessingException {
        // given
        var objectMapper = new ObjectMapper();
        var helloRequest = GreeterOuterClass.HelloRequest.newBuilder()
                .setName("olex")
                .build();
        var helloResponse = GreeterOuterClass.HelloReply.newBuilder()
                .setMessage("Hi!")
                .build();

        // when
        Stub simpleStub = Stub.newStub()
                .forService(GreeterGrpc.getServiceDescriptor())
                .withMethod(GreeterGrpc.getSayHelloMethod())
                .forCall(helloRequest)
                .answer(helloResponse);

        // then
        assertThat(simpleStub.getService()).isEqualTo("Greeter");
        assertThat(simpleStub.getMethod()).isEqualTo("SayHello");

        var typeRef = new TypeReference<HashMap<String, String>>() {
        };

        Map<String, String> input = objectMapper.readValue(simpleStub.getInput().equals(), typeRef);
        Map<String, String> expectedInput = Map.of("name", helloRequest.getName());
        assertThat(input).isEqualTo(expectedInput);

        Map<String, String> output = objectMapper.readValue(simpleStub.getOutput().data(), typeRef);
        Map<String, String> expectedOutput = Map.of("message", helloResponse.getMessage());
        assertThat(output).isEqualTo(expectedOutput);
    }
}