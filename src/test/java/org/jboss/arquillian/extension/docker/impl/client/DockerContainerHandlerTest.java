package org.jboss.arquillian.extension.docker.impl.client;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.dockerjava.client.DockerClient;
import com.github.dockerjava.client.model.ContainerCreateResponse;
import com.github.dockerjava.client.model.ContainerInspectResponse;
import com.github.dockerjava.client.model.ExposedPort;
import com.github.dockerjava.client.model.Ports;


public class DockerContainerHandlerTest {
	private static DockerClient dockerClient = null;
	
	@BeforeClass
	public static void init() {
		dockerClient = new DockerClient("http://localhost:2375");
	}
		
	@Test
	public void testStartStop() throws IOException {
		String containerID = null;		
		try {	
			final ExposedPort tcp888 = ExposedPort.tcp(888);
			final ExposedPort tcp999 = ExposedPort.tcp(999);
			final Ports portBindings = new Ports();
			portBindings.bind(tcp888, Ports.Binding(8080));
			portBindings.bind(tcp999, Ports.Binding(9990));
			
			final ContainerCreateResponse container = dockerClient.createContainerCmd("jboss/wildfly").withExposedPorts(tcp888, tcp999).exec();			
			containerID = container.getId();
			dockerClient.startContainerCmd(containerID).exec();
			final ContainerInspectResponse inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
			inspectContainerResponse.getCreated();
		} finally {
			if (containerID != null) {
				dockerClient.stopContainerCmd(containerID).exec();
				dockerClient.killContainerCmd(containerID).exec();
			}
		}
	}

}
