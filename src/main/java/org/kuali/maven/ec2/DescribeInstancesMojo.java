package org.kuali.maven.ec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.kuali.maven.ec2.pojo.Column;
import org.kuali.maven.ec2.pojo.Row;
import org.kuali.maven.ec2.pojo.RowComparator;
import org.kuali.maven.ec2.pojo.Table;

import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Instance;

/**
 * Connect to EC2 and list instances associated with this account
 *
 * @goal describeinstances
 */
public class DescribeInstancesMojo extends AbstractEC2Mojo {

    /**
     * The tag to display for each instance.
     *
     * @parameter property="ec2.tag" default-value="Name"
     */
    private String tag;

    /**
     * A list of instance id's to display information about
     *
     * @parameter
     */
    private List<String> instanceIds;

    @Override
    public void execute(EC2Utils ec2Utils) throws MojoExecutionException {
        List<Instance> instances = ec2Utils.getEC2Instances(instanceIds);
        Table table = getTable(instances, ec2Utils);
        getLog().info(getDisplay(table.getColumns()));
        for (Row row : table.getRows()) {
            getLog().info(getDisplay(table, row));
        }
    }

    protected String getDisplay(List<Column> columns) {
        StringBuilder sb = new StringBuilder();
        for (Column c : columns) {
            sb.append(StringUtils.rightPad(c.getTitle(), c.getWidth(), " ") + "  ");
        }
        return sb.toString();
    }

    protected String getDisplay(Table table, Row row) {
        StringBuilder sb = new StringBuilder();
        List<String> elements = row.getElements();
        for (int i = 0; i < elements.size(); i++) {
            int width = table.getColumns().get(i).getWidth();
            sb.append(StringUtils.rightPad(elements.get(i), width, " ") + "  ");
        }
        return sb.toString();
    }

    protected String getSecurityGroupsDisplay(Instance i) {
        List<GroupIdentifier> groups = i.getSecurityGroups();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < groups.size(); j++) {
            if (j != 0) {
                sb.append(",");
            }
            GroupIdentifier group = groups.get(j);
            sb.append(group.getGroupName());
        }
        return sb.toString();
    }

    protected List<Row> getRows(List<Instance> instances, EC2Utils ec2Utils) {
        List<Row> rows = new ArrayList<Row>();
        for (Instance i : instances) {
            Row row = new Row();
            List<String> elements = new ArrayList<String>();
            elements.add(ec2Utils.getTagValue(i, tag));
            elements.add(i.getInstanceId());
            elements.add(i.getImageId());
            elements.add(i.getPlacement().getAvailabilityZone());
            elements.add(i.getInstanceType());
            elements.add(i.getState().getName());
            elements.add(getSecurityGroupsDisplay(i));
            elements.add(i.getKeyName());
            row.setElements(elements);
            rows.add(row);
        }
        Collections.sort(rows, new RowComparator());
        Collections.reverse(rows);
        return rows;
    }

    protected List<Column> getColumns(List<Row> rows) {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column(tag));
        columns.add(new Column("Instance"));
        columns.add(new Column("AMI"));
        columns.add(new Column("Availability Zone"));
        columns.add(new Column("Type"));
        columns.add(new Column("State"));
        columns.add(new Column("Security Groups"));
        columns.add(new Column("Key Pair"));
        setWidths(columns, rows);
        return columns;
    }

    protected void setWidths(List<Column> columns, List<Row> rows) {
        for (int i = 0; i < rows.size(); i++) {
            List<String> elements = rows.get(i).getElements();
            for (int j = 0; j < elements.size(); j++) {
                Column c = columns.get(j);
                c.setWidth(Math.max(c.getWidth(), elements.get(j).length()));
            }
        }
    }

    protected Table getTable(List<Instance> instances, EC2Utils ec2Utils) {
        Table table = new Table();
        table.setRows(getRows(instances, ec2Utils));
        table.setColumns(getColumns(table.getRows()));
        return table;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String displayTag) {
        this.tag = displayTag;
    }

}
