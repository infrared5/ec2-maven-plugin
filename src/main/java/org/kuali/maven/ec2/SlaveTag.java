package org.kuali.maven.ec2;

public class SlaveTag implements Comparable<SlaveTag> {
    String key;
    String label;
    String date;
    int sequence;
    String imageId;
    String snapshotId;

    public int compareTo(SlaveTag other) {
        Integer one = this.getSequence();
        Integer two = other.getSequence();
        return one.compareTo(two);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
