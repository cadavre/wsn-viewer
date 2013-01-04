
package pl.cadavre.wsnv.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import pl.cadavre.wsnv.DatabaseConstants;
import android.util.Log;

/**
 * Result representation base Class, one row is one Result
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class Health {

    private Node node;

    private int nodeId = -1;

    private int parentId = -1;

    private Calendar time;

    private int battery;

    private int healthPktsCount;

    private int nodePktsCount;

    public Health(Node node) {

        this(node, null);
    }

    public Health(Node node, ResultSet results) {

        this.node = node;
        this.nodeId = node.getId();
        try {
            setBattery(results.getInt(DatabaseConstants.Health.BATTERY));
            setHealthPktsCount(results.getInt(DatabaseConstants.Health.HEALTH_PACKETS_COUNT));
            setNodePktsCount(results.getInt(DatabaseConstants.Health.NODE_PACKETS_COUNT));
            setParentId(results.getInt(DatabaseConstants.Health.PARENT_ID));
        } catch (SQLException e) {
            Log.e("WSNV", "Error: setting Result object");
            e.printStackTrace();
        } catch (NullPointerException e) {
            // do nothing
        }
    }

    /**
     * @return the node
     */
    public final Node getNode() {

        return node;
    }

    /**
     * @param node the node to set
     */
    public final void setNode(Node node) {

        this.node = node;
    }

    /**
     * @return the nodeId
     */
    public final int getNodeId() {

        return nodeId;
    }

    /**
     * @param nodeId the nodeId to set
     */
    public final void setNodeId(int nodeId) {

        this.nodeId = nodeId;
    }

    /**
     * @return the parentId
     */
    public int getParentId() {

        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(int parentId) {

        this.parentId = parentId;
    }

    /**
     * @return the time
     */
    public final Calendar getTime() {

        return time;
    }

    /**
     * @param time the time to set
     */
    public final void setTime(Calendar time) {

        this.time = time;
    }

    /**
     * @param time the time to set
     */
    public final void setTime(Timestamp time) {

        Calendar readingTime = Calendar.getInstance();
        readingTime.setTimeInMillis(time.getTime());
        setTime(readingTime);
    }

    /**
     * @return the battery
     */
    public int getBattery() {

        return battery;
    }

    /**
     * @param battery the battery to set
     */
    public void setBattery(int battery) {

        this.battery = battery;
    }

    /**
     * @return the healthPktsCount
     */
    public int getHealthPktsCount() {

        return healthPktsCount;
    }

    /**
     * @param healthPktsCount the healthPktsCount to set
     */
    public void setHealthPktsCount(int healthPktsCount) {

        this.healthPktsCount = healthPktsCount;
    }

    /**
     * @return the nodePktsCount
     */
    public int getNodePktsCount() {

        return nodePktsCount;
    }

    /**
     * @param nodePktsCount the nodePktsCount to set
     */
    public void setNodePktsCount(int nodePktsCount) {

        this.nodePktsCount = nodePktsCount;
    }

    public String getConvertedBattery() {

        return String.format("%.2f", (this.battery / 10.0)) + "V";
    }

    @Override
    public String toString() {

        return "Health for node#" + this.nodeId + " " + getConvertedBattery();
    }
}
