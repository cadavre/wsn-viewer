
package pl.zeman.iqh.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import pl.zeman.iqh.DatabaseConstants;
import pl.zeman.iqh.type.LightType;
import pl.zeman.iqh.type.MoveType;
import pl.zeman.iqh.type.TemperatureType;
import android.util.Log;

/**
 * Result representation base Class, one row is one Result
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class Result {

    private Node node;

    private int nodeId = -1;

    private Calendar time;

    private int temperature;

    private int light;

    private int move;

    public Result(Node node) {

        this(node, null);
    }

    public Result(Node node, ResultSet results) {

        this.node = node;
        this.nodeId = node.getId();
        try {
            setTemperature(results.getInt(DatabaseConstants.Results.TEMPERATURE));
            setLight(results.getInt(DatabaseConstants.Results.LIGHT));
            setMove(results.getInt(DatabaseConstants.Results.MOVE));
            setTime(results.getTimestamp(DatabaseConstants.Results.TIMESTAMP));
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
     * @return the temperature
     */
    public final int getTemperature() {

        return temperature;
    }

    /**
     * @param temperature the temperature to set
     */
    public final void setTemperature(int temperature) {

        this.temperature = temperature;
    }

    /**
     * @return the light
     */
    public final int getLight() {

        return light;
    }

    /**
     * @param light the light to set
     */
    public final void setLight(int light) {

        this.light = light;
    }

    /**
     * @return the move
     */
    public final int getMove() {

        return move;
    }

    /**
     * @param move the move to set
     */
    public final void setMove(int move) {

        this.move = move;
    }

    public String getConvertedTemperature() {

        TemperatureType type = new TemperatureType();
        type.setCelsius();

        return String.format("%.2f", type.convert(this.temperature)) + type.unitShort;
    }

    public int getLightLevel() {

        LightType type = new LightType();

        return type.getLevel(this.light);
    }

    public int getMoveStatus() {

        MoveType type = new MoveType();

        return type.getStatus(this.move);
    }
    
    @Override
    public String toString() {

        return "Result for node#" + this.nodeId;
    }
}
