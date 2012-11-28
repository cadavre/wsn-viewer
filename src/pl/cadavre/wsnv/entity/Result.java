
package pl.cadavre.wsnv.entity;

import java.sql.Date;

import pl.cadavre.wsnv.type.Type;

/**
 * Result Class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class Result {

    private double value;

    private Date timestamp;

    private Type type;

    public Result(double value, Type type, Date timestamp) {

        this.value = value;
        this.type = type;
        this.timestamp = timestamp;
    }

    /**
     * @return the value
     */
    public final double getValue() {

        return value;
    }

    /**
     * @param value the value to set
     */
    public final void setValue(double value) {

        this.value = value;
    }

    /**
     * @return the timestamp
     */
    public final Date getTimestamp() {

        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public final void setTimestamp(Date timestamp) {

        this.timestamp = timestamp;
    }

    /**
     * @return Type
     */
    public Type getType() {

        return type;
    }

    /**
     * @param Type
     */
    public void setType(Type type) {

        this.type = type;
    }

}
