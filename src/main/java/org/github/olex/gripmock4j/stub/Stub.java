package org.github.olex.gripmock4j.stub;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.MethodDescriptor;
import io.grpc.ServiceDescriptor;

public class Stub {

    private final String method;
    private final String service;
    private final Equal input;
    private final OutputData output;

    private Stub(String method, String service, String input, String output) {
        this.method = method;
        this.service = service;
        this.input = new Equal(input);
        this.output = new OutputData(output);
    }

    public String getMethod() {
        return method;
    }

    public String getService() {
        return service;
    }

    public Equal getInput() {
        return input;
    }

    public OutputData getOutput() {
        return output;
    }

    public static Builder newStub() {
        return new Builder();
    }

    public static class Builder {

        private ServiceDescriptor serviceDescriptor;
        private MethodDescriptor<? extends GeneratedMessageV3, ? extends GeneratedMessageV3> methodDescriptor;

        private String input;
        private String output;

        private Builder() {
        }

        public Builder forService(ServiceDescriptor serviceDescriptor) {
            this.serviceDescriptor = serviceDescriptor;
            return this;
        }

        public Builder withMethod(
                MethodDescriptor<? extends GeneratedMessageV3, ? extends GeneratedMessageV3> methodDescriptor
        ) {
            this.methodDescriptor = methodDescriptor;
            return this;
        }

        public Builder forCall(MessageOrBuilder request) {
            try {
                this.input = JsonFormat.printer().print(request);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Stub answer(MessageOrBuilder response) {
            try {
                this.output = JsonFormat.printer().print(response);
                return this.build();
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }

        private Stub build() {
            return new Stub(
                    this.methodDescriptor.getBareMethodName(),
                    this.serviceDescriptor.getName(),
                    input,
                    output
            );
        }

    }
}
