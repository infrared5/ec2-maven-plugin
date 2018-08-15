package org.kuali.maven.ec2.state;

import org.kuali.maven.ec2.EC2Utils;
import org.springframework.util.Assert;

import com.amazonaws.services.ec2.model.Instance;

public class InstanceStateRetriever implements StateRetriever {

    EC2Utils ec2Utils;
    String instanceId;

    public InstanceStateRetriever() {
        this(null, null);
    }

    public InstanceStateRetriever(EC2Utils ec2Utils, String instanceId) {
        super();
        this.ec2Utils = ec2Utils;
        this.instanceId = instanceId;
    }

    public String getState() {
        Assert.notNull(ec2Utils);
        Assert.notNull(instanceId);
        Instance i = ec2Utils.getEC2Instance(instanceId);
        return i.getState().getName();
    }

    public EC2Utils getEc2Utils() {
        return ec2Utils;
    }

    public void setEc2Utils(EC2Utils ec2Utils) {
        this.ec2Utils = ec2Utils;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

}
