package org.kuali.maven.ec2.pojo;

import java.util.Comparator;

import com.amazonaws.services.ec2.model.Image;

public class ImageComparator implements Comparator<Image> {

    public int compare(Image one, Image two) {
        String location1 = one.getImageLocation();
        String location2 = two.getImageLocation();
        return location1.compareTo(location2);
    }

}
