package org.kuali.maven.ec2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.amazonaws.services.ec2.model.Tag;

public class LaunchInstanceMojoTest {

	MavenProject project;
	private static final Logger logger = LoggerFactory.getLogger(EC2UtilsTest.class);

	@BeforeEach
	public void beforeAll() {
		project = mock(MavenProject.class);
		when(project.getProperties()).thenReturn(new Properties());

	}

	@Test
	public void testExecute() throws Exception {
		start_then_terminate_instance();		
	}

	@Test
	@Disabled
	public void testExecute_hundred() throws Exception {
		for(int i=0; i<100; i++) {
			start_then_terminate_instance();
		}
	}

	private void start_then_terminate_instance() throws MojoExecutionException {
		logger.info("Start instance...");
		LaunchInstanceMojo launch = new LaunchInstanceMojo();
		launch.setProject(project);
		launch.setAmi("ami-0cf31d971a3ca20d6"); // Amazon Linux 2 AMI (HVM), SSD Volume Type
		launch.setKey("gbandsmith");
		launch.setType("t2.micro");
		List<String> securityGroups = new ArrayList<String>();
		securityGroups.add("bo");
		securityGroups.add("ssh");
		launch.setSecurityGroups(securityGroups);
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag("Name", "ec2-maven-plugin-test"));
		tags.add(new Tag("description", "ec2-maven-plugin-test"));
		tags.add(new Tag("costcenter", "fenix"));
		tags.add(new Tag("pool", "INTEGRATION"));
		launch.setTags(tags);
		launch.execute(Common.getEC2Utils());
		String instanceId = project.getProperties().getProperty("ec2.instance.id");
		Assert.notNull(instanceId);
		logger.info("instance " + instanceId + " started.");

		logger.info("Terminate instance...");
		TerminateInstanceMojo terminate = new TerminateInstanceMojo();
		terminate.setInstanceId(instanceId);
		terminate.execute(Common.getEC2Utils());
		logger.info("instance " + instanceId + " terminated.");
	}

}
