package org.github.olex.gripmock4j;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;


public class GripmockContainer extends GenericContainer<GripmockContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("tkpd/gripmock");
    private static final String DEFAULT_TAG = "v1.12.1";

    private static final int grpcServerPort = 4770;
    private static final int httpServerPort = 4771;

    public GripmockContainer(String... protoPaths) {
        this(DockerImageName.parse(DEFAULT_IMAGE_NAME.asCanonicalNameString() + ":" + DEFAULT_TAG), protoPaths);
    }

    public GripmockContainer(DockerImageName dockerImageName, String... protoPaths) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        waitingFor(Wait.forListeningPort());
        withExposedPorts(4770, 4771);
        withCommand(protoPaths);
    }

    public String getStubAdminAddress() {
        return "http://" + getHost() + ":" + getHttpServerPort();
    }

    public String getGrpcServerAddress() {
        return getHost() + ":" + getGrpcServerPort();
    }

    public int getHttpServerPort() {
        return getMappedPort(httpServerPort);
    }

    public int getGrpcServerPort() {
        return getMappedPort(grpcServerPort);
    }

}
