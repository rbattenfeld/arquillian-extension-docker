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
	
	public void startDockerContainer(@Observes(precedence = 1) ManagerStarted event) {		
		startDockerContainer();
	}

	public void stopDockerContainer(@Observes ManagerStopping event) {
		stopDockerContainer();
	}

	public void wait(@Observes BeforeSuite event) {
		Thread.currentThread();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
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

	private ArquillianDescriptor loadFromArquillianXml() {
		final InputStream arqXmlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ARQUILLIAN_XML);
		return Descriptors.importAs(ArquillianDescriptor.class).fromStream(arqXmlStream);
	}
	
}
