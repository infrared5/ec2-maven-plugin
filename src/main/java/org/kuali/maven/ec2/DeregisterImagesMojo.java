package org.kuali.maven.ec2;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Connect to EC2, deregister images (and delete their associated snapshots) that have matching tags
 * 
 * @goal deregisterimages
 */
public class DeregisterImagesMojo extends AbstractEC2Mojo {

    /**
     * This is the name of the tag to match.
     * 
     * @parameter property="ec2.key" default-value="Name"
     */
    private String key;

    /**
     * If the specified tag exists, and its value starts with the text provided here, the associated image will be
     * deregistered.
     * 
     * @parameter property="ec2.prefix" default-value="CI Slave"
     */
    private String prefix;

    /**
     * The name of the device whose associated snapshot will be deleted when the image is deregistered.
     * 
     * @parameter property="ec2.device" default-value="/dev/sda1"
     */
    private String device;

    /**
     * The minimum number of images to retain.
     * 
     * @parameter property="ec2.minimumToRetain" default-value="3"
     */
    private int minimumToRetain;

    @Override
    public void execute(EC2Utils ec2Utils) throws MojoExecutionException {
        ec2Utils.cleanupSlaveImages(key, prefix, device, minimumToRetain);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public int getMinimumToRetain() {
        return minimumToRetain;
    }

    public void setMinimumToRetain(int minimumToRetain) {
        this.minimumToRetain = minimumToRetain;
    }

}