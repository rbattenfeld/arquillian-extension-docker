Docker Integration for the Arquillian Project

Allows to execute arquillian remote container tests against a docker container. 

Usage
-----
This extension is working in conjunction with the other arquillian remote containers. Instead of a running remote application server
instance, this extension allows to manage a docker container, in which then a remote application server is running. 

This extension does very little :

1. Starts very early in the test execution a docker container instance.
2. Sets then the managementAddress system property with the real docker container IP address.
3.  --- test execution covered by the specified arquillian remote container ---
4. Stops and removes the container instance at the end of the test.


Configuration:
-------------

Add the docker information to the arquillian.xml descriptor. Example:

<?xml version="1.0" encoding="UTF-8"?>
<arquillian xmlns="http://jboss.org/schema/arquillian"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd">

	<defaultProtocol type="jmx-as7" />

	<container qualifier="jboss" default="true">
		<configuration>
			<property name="managementAddress">${arquillian.docker.address}</property>
			<property name="managementPort">9990</property>
			<property name="username">admin</property>
			<property name="password">Admin#70365</property>			
		</configuration>
	</container>

	<extension qualifier="docker">
		<property name="dockerImage">jboss/wildfly-admin</property>
		<property name="dockerHost">localhost</property>
		<property name="dockerPort">2375</property>
	</extension>

</arquillian>

Important: The variable ${arquillian.docker.address} must be set like this. This address will be replaced
with the real docker container IP address.


Prerequisite:
------------

A working docker installation available as specified with the properties dockerHost and dockerPort. And, the docker port is available as TCP port as described here in
Bind Docker to another host/port or a Unix socket Link: http://docs.docker.com/articles/basics/#bind-docker


