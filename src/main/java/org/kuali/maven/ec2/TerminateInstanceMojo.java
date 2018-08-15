package org.kuali.maven.ec2;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Connect to EC2 and terminate the indicated instance.
 *
 * @goal terminateinstance
 */
public class TerminateInstanceMojo extends AbstractEC2Mojo {

    /**
     * The id of the instance to terminate. Set this to <code>NONE</code> to skip attempting to terminate an instance
     *
     * @parameter property="ec2.instanceId"
     * @required
     */
    private String instanceId;

    /**
     * If true, the build will wait until EC2 reports that the instance has reached the state of "terminated"
     *
     * @parameter property="ec2.wait" default-value="false"
     */
    private boolean wait;

    /**
     * The number of seconds to wait for the instance to terminate before timing out and failing the build
     *
     * @parameter property="ec2.waitTimeout" default-value="300"
     */
    private int waitTimeout;

    /**
     * The state the instance needs to be in before the plugin considers it to be terminated.
     *
     * @parameter property="ec2.state" default-value="terminated"
     */
    private String state;

    @Override
    protected boolean isSkip() {
        if (Constants.NONE.equals(instanceId)) {
            getLog().info("instanceId=" + instanceId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void execute(EC2Utils ec2Utils) throws MojoExecutionException {
        WaitControl wc = new WaitControl(wait, waitTimeout, state);
        ec2Utils.terminate(instanceId, wc);
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String id) {
        this.instanceId = id;
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
}
