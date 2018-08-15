package org.kuali.maven.ec2;

import org.apache.maven.plugin.MojoExecutionException;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;

/**
 * Connect to EC2 and find a single instance based on a tag/value pair
 *
 * @goal findinstance
 */
public class FindInstanceMojo extends AbstractEC2Mojo {
    private static final String NONE = Constants.NONE;

    /**
     * The name of the tag to search for
     *
     * @parameter property="ec2.tag" default-value="Name"
     * @required
     */
    private String tag;

    /**
     * The value that tag needs to have
     *
     * @parameter property="ec2.value"
     */
    private String value;

    /**
     * If true, fail the build when no matching instance is found
     *
     * @parameter property="ec2.failIfNotFound" default-value="false"
     * @required
     */
    private boolean failIfNotFound;

    /**
     * If supplied, the id of the instance located by the plugin is stored as this project property. If no matching
     * instance is located, the property is set to the value <code>NONE</code>.
     *
     * @parameter property="ec2.instanceIdProperty" default-value="ec2.instance.id"
     * @required
     */
    private String instanceIdProperty;

    @Override
    public void execute(EC2Utils ec2Utils) throws MojoExecutionException {
        Instance instance = ec2Utils.findInstanceFromTag(new Tag(tag, value), failIfNotFound);
        String id = NONE;
        if (instance != null) {
            id = instance.getInstanceId();
        }
        getLog().info("Setting " + instanceIdProperty + "=" + id);
        project.getProperties().setProperty(instanceIdProperty, id);
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isFailIfNotFound() {
        return failIfNotFound;
    }

    public void setFailIfNotFound(boolean failIfNotFound) {
        this.failIfNotFound = failIfNotFound;
    }

    public String getInstanceIdProperty() {
        return instanceIdProperty;
    }

    public void setInstanceIdProperty(String instanceIdProperty) {
        this.instanceIdProperty = instanceIdProperty;
    }

}
