package org.kuali.maven.ec2.pojo;

public class Column {

    String title;
    int width;

    public Column() {
        this(null);
    }

    public Column(String title) {
        super();
        this.title = title;
        this.width = title == null ? 0 : title.length();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
