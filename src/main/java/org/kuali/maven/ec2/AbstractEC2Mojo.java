package org.kuali.maven.ec2;

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.amazonaws.services.ec2.model.Tag;

public abstract class AbstractEC2Mojo extends AbstractMojo {

    /**
     * The Maven project object
     *
     * @parameter property="project"
     * @readonly
     */
    MavenProject project;



	/**
     * List of tags. Tags are name/value pairs and can be supplied in the plugin configuration like this:<br>
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
    List<Tag> tags;

    /**
     * The AWS Access Key Id for an account on EC2
     *
     * @parameter property="ec2.accessKey"
     * @required
     */
    String accessKey;

    /**
     * The AWS Secret Access Key for an account on EC2
     *
     * @parameter property="ec2.secretKey"
     * @required
     */
    String secretKey;

    protected boolean isSkip() {
        return false;
    }

    public abstract void execute(EC2Utils ec2Utils) throws MojoExecutionException;

    public void execute() throws MojoExecutionException {
        if (isSkip()) {
            getLog().info("Skipping execution");
            return;
        }
        EC2Utils ec2Utils = EC2Utils.getInstance(accessKey, secretKey);
        execute(ec2Utils);
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public MavenProject getProject() {
        return project;
    }
    
    public void setProject(MavenProject project) {
		this.project = project;
	}

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

}
