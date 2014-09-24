/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.extension.docker.impl.client;

import java.io.InputStream;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.core.api.event.ManagerStopping;
import org.jboss.arquillian.extension.docker.impl.common.DockerConfiguration;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

import com.github.dockerjava.client.DockerClient;
import com.github.dockerjava.client.model.ContainerCreateResponse;
import com.github.dockerjava.client.model.ContainerInspectResponse;

/**
 * MailServerInstaller
 * 
 * @author <a href="mailto:ralf.battenfeld@bluewin.ch">Ralf Battenfeld</a>
 * @version $Revision: $
 */
public class DockerContainerHandler {

	private static final Logger log = Logger.getLogger(DockerContainerHandler.class.getName());
	
	private static final String ARQUILLIAN_XML = "arquillian.xml";

	private DockerClient dockerClient = null;

	private ContainerCreateResponse containerCreateResponse = null;
	
	/**
	 * Starts the container before the arquillian descriptor is parsed and loaded by arquillian.
	 * We need at this point to fetch the IP address of the started docker container.
	 * <p>
	 * The remote container then will connect to the running Java EE container instance for deploying and executing the tests.
	 * @param event
	 */
	public void startDockerContainer(@Observes(precedence = 1) ManagerStarted event) {		
		startDockerContainer();
	}

	public void stopDockerContainer(@Observes ManagerStopping event) {
		stopDockerContainer();
	}

	/**
	 * We have to wait a little bit. 
	 * @param event
	 */
	public void wait(@Observes BeforeSuite event) {
		Thread.currentThread();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			log.warning(e.getMessage()); // TODO poll for container state
		}
	}
	
	// -----------------------------------------------------------------------||
	// -- Private Methods ----------------------------------------------------||
	// -----------------------------------------------------------------------||

	private void startDockerContainer() {
		log.info("Starting docker container ...");
		final DockerConfiguration config = DockerConfiguration.from(loadFromArquillianXml());
		dockerClient = new DockerClient(String.format("http://%s:%d", config.dockerHost(), config.dockerPort()));
		containerCreateResponse = dockerClient.createContainerCmd(config.dockerImage()).exec();
		log.info("Created container with ID: " + containerCreateResponse.getId());

		dockerClient.startContainerCmd(containerCreateResponse.getId()).exec();
		ContainerInspectResponse response = dockerClient.inspectContainerCmd(containerCreateResponse.getId()).exec();
		log.info("Docker container ip address: " + response.getNetworkSettings().getIpAddress());	

		System.getProperties().setProperty("arquillian.docker.address", response.getNetworkSettings().getIpAddress());
		log.info("Starting docker container done");
	}
	
	private void stopDockerContainer() {
		log.info("Stopping docker container ...");
		if (containerCreateResponse != null) {
			dockerClient.stopContainerCmd(containerCreateResponse.getId()).exec();
            dockerClient.removeContainerCmd(containerCreateResponse.getId()).exec();
		}
		log.info("Stopping docker container done");
	}

	/**
	 * Loads the arquillian descriptor. This is required because we have to set the system properties before the 
	 * descriptor is loaded and parsed.
	 * @return
	 */
	private ArquillianDescriptor loadFromArquillianXml() {
		final InputStream arqXmlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ARQUILLIAN_XML);
		return Descriptors.importAs(ArquillianDescriptor.class).fromStream(arqXmlStream);
	}
	
}
