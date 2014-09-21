/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.extension.docker.impl.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.shrinkwrap.impl.base.io.IOUtil;

/**
 * DockerConfiguration
 *
 * @author <a href="mailto:ralf.battenfeld@bluewin.ch">Ralf Battenfeldn</a>
 * @version $Revision: $
 */
public class DockerConfiguration {
	public static String DOCKER_HOST = "dockerHost";
	public static String DOCKER_PORT = "dockerPort";
	public static String DOCKER_IMAGE = "dockerImage";
	public static String DOCKER_EXTENSION_NAME = "docker";

	private Map<String, String> properties;

	public DockerConfiguration(final Map<String, String> properties) {
		this.properties = properties;
	}

	public String dockerHost() {
		return properties.get(DOCKER_HOST);
	}

	public Integer dockerPort() {
		return Integer.valueOf(properties.get(DOCKER_PORT));
	}
	
	public String dockerImage() {
		return properties.get(DOCKER_IMAGE);
	}

	@Override
	public String toString() {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			Properties tmp = new Properties();
			tmp.putAll(properties);
			tmp.store(output, "docker-auto-added");
		} catch (IOException e) {
			// no-op, what could possible go wrong ? ;)
		}
		return output.toString();
	}

	public static DockerConfiguration from(ArquillianDescriptor descriptor) {
		return new DockerConfiguration(locateDockerExtension(descriptor));
	}

	public static DockerConfiguration from(InputStream inputStream) {
		return from(IOUtil.asUTF8String(inputStream));
	}

	public static DockerConfiguration from(String properties) {
		return new DockerConfiguration(loadPropertiesString(properties));
	}
    
	//-----------------------------------------------------------------------||
	//-- Private Methods ----------------------------------------------------||
	//-----------------------------------------------------------------------||

	private static Map<String, String> locateDockerExtension(final ArquillianDescriptor descriptor) {
		if (descriptor != null) {
			for (ExtensionDef extension : descriptor.getExtensions()) {
				if (DOCKER_EXTENSION_NAME.equalsIgnoreCase(extension.getExtensionName())) {
					return extension.getExtensionProperties();
				}
			}
		}
		return new HashMap<String, String>();
	}

	private static Map<String, String> loadPropertiesString(final String properties) {
		final Map<String, String> result = new HashMap<String, String>();
		final Properties props = new Properties();
		try {
			props.load(new StringReader(properties));
		} catch (IOException e) {
			// no-op, no IOException in StringReader
		}
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
		}
		return result;
	}
}
