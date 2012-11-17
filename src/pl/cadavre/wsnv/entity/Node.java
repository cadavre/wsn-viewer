
package pl.cadavre.wsnv.entity;

import pl.cadavre.wsnv.type.Type;

/**
 * Node representation base Class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class Node {

    private int id;

    private Type type;

    private String location;

    private double x;

    private double y;

    private double z;

    /**
     * @return the id
     */
    public final int getId() {

        return id;
    }

    /**
     * @param id the id to set
     */
    public final void setId(int id) {

        this.id = id;
    }

    /**
     * @return the x
     */
    public final double getX() {

        return x;
    }

    /**
     * @param x the x coordinate to set
     */
    public final void setX(double x) {

        this.x = x;
    }

    /**
     * @return the y
     */
    public final double getY() {

        return y;
    }

    /**
     * @param y the y coordinate to set
     */
    public final void setY(double y) {

        this.y = y;
    }

    /**
     * @return the z
     */
    public final double getZ() {

        return z;
    }

    /**
     * @param z the z coordinate to set
     */
    public final void setZ(double z) {

        this.z = z;
    }

    public Type getType() {

        return type;
    }

    public void setType(Type type) {

        this.type = type;
    }

    public String getLocation() {

        return location;
    }

    public void setLocation(String location) {

        this.location = location;
    }

}
