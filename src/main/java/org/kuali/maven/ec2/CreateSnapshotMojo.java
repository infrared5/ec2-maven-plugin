package org.kuali.maven.ec2;

import org.apache.maven.plugin.MojoExecutionException;

import com.amazonaws.services.ec2.model.Snapshot;

/**
 * Create a snapshot of a volume. If successful, the project property <code>ec2.snapshot.id</code> will contain the id
 * of the snapshot that was created.
 *
 * @goal createsnapshot
 */
public class CreateSnapshotMojo extends AbstractEC2Mojo {

    /**
     * The volume to take a snapshot of
     *
     * @parameter property="ec2.volumeId"
     * @required
     */
    private String volumeId;

    /**
     * The description of the snapshot (if any)
     *
     * @parameter property="ec2.description"
     */
    private String description;

    /**
     * If true, the build will wait until EC2 reports that the snapshot has reached the state of "completed"
     *
     * @parameter property="ec2.wait" default-value="true"
     */
    private boolean wait;

    /**
     * The number of seconds to wait for the instance to terminate before timing out and failing the build
     *
     * @parameter property="ec2.waitTimeout" default-value="7200"
     */
    private int waitTimeout;

    /**
     * The number of seconds to sleep in between status checks on the snapshot
     *
     * @parameter property="ec2.sleep" default-value="30"
     */
    private int sleep;

    /**
     * The state the plugin will wait for the snapshot to be in before continuing
     *
     * @parameter property="ec2.state" default-value="completed"
     */
    private String state;

    @Override
    protected boolean isSkip() {
        if (Constants.NONE.equals(volumeId)) {
            getLog().info("volumeId=" + volumeId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void execute(EC2Utils ec2Utils) throws MojoExecutionException {
        WaitControl wc = new WaitControl(wait, waitTimeout, state);
        wc.setSleep(sleep * 1000);
        long start = System.currentTimeMillis();
        Snapshot snapshot = ec2Utils.createSnapshot(volumeId, description, wc);
        long millis = System.currentTimeMillis() - start;
        getLog().info("Elapsed: " + millis / 1000 + "s");
        getLog().info("Setting ec2.snapshot.id=" + snapshot.getSnapshotId());
        project.getProperties().setProperty("ec2.snapshot.id", snapshot.getSnapshotId());
        ec2Utils.tag(snapshot.getSnapshotId(), tags);
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

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }
}
