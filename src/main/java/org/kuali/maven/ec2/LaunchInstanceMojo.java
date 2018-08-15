package org.kuali.maven.ec2;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;
import org.kuali.maven.common.PropertiesUtils;
import org.kuali.maven.common.ResourceUtils;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;

/**
 * Connect to EC2 and launch a a single instance configured according to user preferences. By default, the plugin waits until the instance
 * reaches the state of "running" before allowing the build to continue. Once an EC2 instance is "running" Amazon has assigned it a public
 * dns name. The public dns name, the instance id, and the value of the tag "Name" (if that tag is supplied) are stored as the project
 * properties <code>ec2.instance.dns</code>, <code>ec2.instance.id</code>, <code>ec2.instance.name</code>, respectively.
 * 
 * If <code>wait</code> is false, the <code>ec2.instance.dns</code> property will not be set since the instance will not have a public dns
 * name by the time the plugin execution completes.
 * 
 * @goal launchinstance
 */
public class LaunchInstanceMojo extends AbstractEC2Mojo {

	/**
	 * The AMI to launch
	 * 
	 * @parameter property="ec2.ami"
	 * @required
	 */
	private String ami;

	/**
	 * The name of the key to use
	 * 
	 * @parameter property="ec2.key"
	 * @required
	 */
	private String key;

	/**
	 * The type of instance to launch
	 * 
	 * @parameter property="ec2.type" default-value="t2.medium";
	 * @required
	 */
	private String type;

	/**
	 * The security groups into which the instance will be launched
	 * 
	 * @parameter
	 */
	private List<String> securityGroups;

	/**
	 * Optional user data for the instance
	 * 
	 * @parameter property="ec2.userData"
	 */
	private String userData;

	/**
	 * If supplied, the contents of the file are supplied to the instance as userData. This can be a file on the file system, or any url
	 * Spring resource loading can understand eg "<code>classpath:user-data.txt</code>"
	 * 
	 * @parameter property="ec2.userDataFile"
	 */
	private String userDataFile;

	/**
	 * If true, userData is filtered with current project, environment, and system properties before being supplied to the instance.
	 * 
	 * @parameter property="ec2.filterUserData"
	 */
	private boolean filterUserData;

	/**
	 * The encoding of the userDataFile
	 * 
	 * @parameter property="ec2.encoding" default-value="${project.build.sourceEncoding}"
	 */
	private String encoding;

	/**
	 * If true, the build will wait until EC2 reports that the instance has reached the state of "running" before continuing
	 * 
	 * @parameter property="ec2.wait" default-value="true"
	 */
	private boolean wait;

	/**
	 * The number of seconds to wait for the instance to start before timing out and failing the build
	 * 
	 * @parameter property="ec2.waitTimeout" default-value="300"
	 */
	private int waitTimeout;
	
	/**
	 * The port number to wait to be open before continuing (0 = no test)
	 * 
	 * @parameter property="ec2.waitPort" default-value="0"
	 */
	private int waitPort;

	/**
	 * The state the instance needs to be in before the plugin considers it to be started.
	 * 
	 * @parameter property="ec2.state" default-value="running"
	 */
	private String state;

	/**
	 * Wait for this amount of time before attempting to run an instance. Amazon needs about 1/2 a second to sort itself out internally or
	 * the run request will fail
	 * 
	 * @parameter property="ec2.initialPause" default-value="3000"
	 */
	private int initialPause;

	@Override
	public void execute(EC2Utils ec2Utils) throws MojoExecutionException {
		EC2Utils.sleep(initialPause);
		RunInstancesRequest request = getRunSingleEC2InstanceRequest();
		Instance i = ec2Utils.getSingleEC2Instance(request);
		ec2Utils.createTags(i, tags);
		WaitControl wc = new WaitControl(wait, waitTimeout, waitPort, state);
		Properties props = project.getProperties();
		Instance running = ec2Utils.wait(i, wc, props);
		props.setProperty("ec2.instance.id", running.getInstanceId());
		props.setProperty("ec2.instance.name", ec2Utils.getTagValue(running, "Name"));
	}

	protected RunInstancesRequest getRunSingleEC2InstanceRequest() throws MojoExecutionException {
		RunInstancesRequest request = new RunInstancesRequest(ami, 1, 1);
		request.setKeyName(key);
		request.setInstanceType(InstanceType.fromValue(type));
		request.setSecurityGroups(securityGroups);
		String data = getUserData(userData, userDataFile, encoding);
		request.setUserData(data);
		return request;
	}

	protected String getUserData(String data, String location, String encoding) throws MojoExecutionException {
		String s = data;
		if (!StringUtils.isBlank(location)) {
			try {
				s = getString(location, encoding);
			} catch (IOException e) {
				throw new MojoExecutionException("Error reading from " + location, e);
			}
		}
		if (StringUtils.isBlank(s)) {
			return null;
		}
		if (filterUserData) {
			PropertiesUtils pu = new PropertiesUtils();
			Properties properties = pu.getMavenProperties(project);
			s = pu.getResolvedValue(s, properties);
		}
		getLog().debug("filteredUserData=" + s);
		byte[] bytes = Base64.encodeBase64(s.getBytes());
		String base64 = new String(bytes);
		getLog().debug("base64Encoded=" + base64);
		return base64;
	}

	protected String getString(String location, String encoding) throws IOException {
		InputStream in = null;
		try {
			ResourceUtils ru = new ResourceUtils();
			in = ru.getInputStream(location);
			return IOUtils.toString(in, encoding);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public String getAmi() {
		return ami;
	}

	public void setAmi(String ami) {
		this.ami = ami;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getSecurityGroups() {
		return securityGroups;
	}

	public void setSecurityGroups(List<String> securityGroups) {
		this.securityGroups = securityGroups;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public boolean isFilterUserData() {
		return filterUserData;
	}

	public void setFilterUserData(boolean filterUserData) {
		this.filterUserData = filterUserData;
	}

	public boolean isWait() {
		return wait;
	}

	public void setWait(boolean wait) {
		this.wait = wait;
	}

	public int getWaitTimeout() {
		return waitTimeout;
	}

	public void setWaitTimeout(int waitTimeout) {
		this.waitTimeout = waitTimeout;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getUserDataFile() {
		return userDataFile;
	}

	public void setUserDataFile(String userDataFile) {
		this.userDataFile = userDataFile;
	}

	public int getWaitPort() {
		return waitPort;
	}

	public void setWaitPort(int waitPort) {
		this.waitPort = waitPort;
	}
}
