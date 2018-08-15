package org.kuali.maven.ec2.pojo;

import java.util.List;

public class Table {
    List<Column> columns;
    List<Row> rows;

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }
}
