# gripmock4j

Java client for https://github.com/tokopedia/gripmock

## Features

- DSL-like api
- Testcontainers wrapper

## Quickstart usage

1. Setup gripmock server
2. Obtain the [`Gripmock`](src/main/java/org/github/olex/gripmock4j/Gripmock.java) instance
3. Construct the stub with `[Stub.newStub()](src/main/java/org/github/olex/gripmock4j/stub/Stub.java)`
4. Pass it to `Gripmock`

```
var gripmock = new Gripmock(gripmockContainer.getHost(), gripmockContainer.getHttpServerPort());
        
var req = Simple.Request.newBuilder()
    .setName("olex")
    .build();
var res = Simple.Reply.newBuilder()
    .setMessage("hi!")
    .setReturnCode(201)
    .build();

gripmock.addStubMapping(
    Stub.newStub()
        .forService(GripmockGrpc.getServiceDescriptor())
        .withMethod(GripmockGrpc.getSayHelloMethod())
        .forCall(req)
        .answer(res)
)
```

## TODO

- Add `/find` and `/get` support for stubbing lookup
- Move to gson instead of jackson