package org.kuali.maven.ec2.state;

import org.kuali.maven.ec2.EC2Utils;
import org.springframework.util.Assert;

import com.amazonaws.services.ec2.model.Image;

public class ImageStateRetriever implements StateRetriever {

    EC2Utils ec2Utils;
    String imageId;

    public ImageStateRetriever() {
        this(null, null);
    }

    public ImageStateRetriever(EC2Utils ec2Utils, String imageId) {
        super();
        this.ec2Utils = ec2Utils;
        this.imageId = imageId;
    }

    public String getState() {
        Assert.notNull(ec2Utils);
        Assert.notNull(imageId);
        Image i = ec2Utils.getImage(imageId);
        return i.getState();
    }

    public EC2Utils getEc2Utils() {
        return ec2Utils;
    }

    public void setEc2Utils(EC2Utils ec2Utils) {
        this.ec2Utils = ec2Utils;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

}
