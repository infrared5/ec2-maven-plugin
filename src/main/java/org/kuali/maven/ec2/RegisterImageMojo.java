package org.kuali.maven.ec2;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.RegisterImageRequest;
import com.amazonaws.services.ec2.model.RegisterImageResult;
import com.amazonaws.services.ec2.model.Tag;

/**
 * Register an Amazon machine image (AMI)
 * 
 * @goal registerimage
 */
public class RegisterImageMojo extends AbstractEC2Mojo {

	/**
	 * The image to register. See <code>com.amazonaws.services.ec2.model.RegisterImageRequest</code>
	 * 
	 * @parameter
	 * @required
	 */
	RegisterImageRequest image;

	/**
	 * The BlockDeviceMappings for the image. See <code>com.amazonaws.services.ec2.model.BlockDeviceMapping</code>
	 * 
	 * @parameter
	 * @required
	 */
	List<BlockDeviceMapping> blockDeviceMappings;

	/**
	 * If true, the build will wait until EC2 reports that the AMI has reached the state of "available"
	 * 
	 * @parameter property="ec2.wait" default-value="true"
	 */
	private boolean wait;

	/**
	 * The number of seconds to wait for the AMI to become available
	 * 
	 * @parameter property="ec2.waitTimeout" default-value="30"
	 */
	private int waitTimeout;

	/**
	 * The state the plugin will wait for the AMI to be in before continuing
	 * 
	 * @parameter property="ec2.state" default-value="available"
	 */
	private String state;

	@Override
	public void execute(EC2Utils ec2Utils) throws MojoExecutionException {
		WaitControl wc = new WaitControl(wait, waitTimeout, state);
		wc.setSleep(1000);
		wc.setInitialPause(Constants.DEFAULT_INITIAL_PAUSE_MILLIS);
		// For some reason, Maven's automatic bean setting logic chokes on RegisterImageRequest.setBlockDeviceMappings()
		// That is the only reason for a separate blockDeviceMappings member variable
		// If the Maven issue gets sorted out, remove the blockDeviceMappings member variable and
		// just configure the RegisterImageRequest directly
		image.setBlockDeviceMappings(blockDeviceMappings);
		RegisterImageResult result = ec2Utils.registerImage(image, wc);
		String imageId = result.getImageId();
		getLog().info("Setting ec2.image.id=" + imageId);
		project.getProperties().setProperty("ec2.image.id", imageId);
		ec2Utils.tag(imageId, tags);
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

	public RegisterImageRequest getImage() {
		return image;
	}

	public void setImage(RegisterImageRequest image) {
		this.image = image;
	}

	public List<BlockDeviceMapping> getBlockDeviceMappings() {
		return blockDeviceMappings;
	}

	public void setBlockDeviceMappings(List<BlockDeviceMapping> blockDeviceMappings) {
		this.blockDeviceMappings = blockDeviceMappings;
	}

	@Override
	public List<Tag> getTags() {
		return tags;
	}

	@Override
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
}
