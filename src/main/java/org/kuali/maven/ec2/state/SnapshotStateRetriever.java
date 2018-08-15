package org.kuali.maven.ec2.state;

import org.kuali.maven.ec2.EC2Utils;
import org.springframework.util.Assert;

import com.amazonaws.services.ec2.model.Snapshot;

public class SnapshotStateRetriever implements StateRetriever {

    EC2Utils ec2Utils;
    String snapshotId;

    public SnapshotStateRetriever() {
        this(null, null);
    }

    public SnapshotStateRetriever(EC2Utils ec2Utils, String snapshotId) {
        super();
        this.ec2Utils = ec2Utils;
        this.snapshotId = snapshotId;
    }

    public String getState() {
        Assert.notNull(ec2Utils);
        Assert.notNull(snapshotId);
        Snapshot s = ec2Utils.getSnapshot(snapshotId);
        return s.getState();
    }

    protected void validate() {
    }

    public EC2Utils getEc2Utils() {
        return ec2Utils;
    }

    public void setEc2Utils(EC2Utils ec2Utils) {
        this.ec2Utils = ec2Utils;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

}
