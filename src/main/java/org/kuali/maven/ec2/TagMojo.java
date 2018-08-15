package org.kuali.maven.ec2;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

import com.amazonaws.services.ec2.model.Tag;

/**
 * Connect to EC2 and tag a resource with name/value pairs. Each resource can have up to 10 tags.
 * <code>resourceId</code> can be an EC2 instance id, volume id, snapshot id, etc.
 *
 * @goal tag
 */
public class TagMojo extends AbstractEC2Mojo {

    /**
     * The id of the resource to tag. Set this to <code>NONE</code> to skip tagging any resources
     *
     * @parameter property="ec2.resourceId"
     * @required
     */
    private String resourceId;

    /**
     * List of tags to associate with the resource. Tags are key value pairs and can be supplied in the plugin
     * configuration like this:<br>
     *
     * <pre>
     *   &lt;tags&gt;
     *     &lt;tag&gt;
     *       &lt;key&gt;Name&lt;/key&gt;
     *       &lt;value&gt;production&lt;/value&gt;
     *     &lt;/tag&gt;
     *     &lt;tag&gt;
     *       &lt;key&gt;Category&lt;/key&gt;
     *       &lt;value&gt;networking&lt;/value&gt;
     *     &lt;/tag&gt;
     *   &lt;/tags&gt;
     * </pre>
     *
     * @parameter
     */
    private List<Tag> tags;

    @Override
    protected boolean isSkip() {
        boolean skip1 = Constants.NONE.equals(resourceId);
        boolean skip2 = EC2Utils.isEmpty(tags);
        if (skip1) {
            getLog().info("Resource Id=" + resourceId);
        }
        if (skip2) {
            getLog().info("No tags were supplied");
        }
        if (skip1 || skip2) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void execute(EC2Utils ec2Utils) throws MojoExecutionException {
        ec2Utils.tag(resourceId, tags);
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String instanceId) {
        this.resourceId = instanceId;
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
