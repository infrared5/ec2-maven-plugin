package org.kuali.maven.ec2.pojo;

import java.util.Comparator;

public class RowComparator implements Comparator<Row> {
    int elementIndex;

    public int compare(Row row1, Row row2) {
        String s1 = row1.getElements().get(elementIndex);
        String s2 = row2.getElements().get(elementIndex);
        return s1.compareTo(s2);
    }

    public int getElementIndex() {
        return elementIndex;
    }

    public void setElementIndex(int elementIndex) {
        this.elementIndex = elementIndex;
    }

}
