
package pl.zeman.iqh.entity;

/**
 * Node representation base Class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class Node {

    private int id; // auto

    private String name; // manual

    private boolean enabled = true;

    private double x = 0; // manual

    private double y = 0; // manual

    private double z = 0; // manual

    public Node() {

    }

    /**
     * Get ID
     * 
     * @return the id
     */
    public final int getId() {

        return id;
    }

    /**
     * Set ID
     * 
     * @param id the id to set
     */
    public final void setId(int id) {

        this.id = id;
    }

    /**
     * Get X
     * 
     * @return the x
     */
    public final double getX() {

        return x;
    }

    /**
     * Set X
     * 
     * @param x the x coordinate to set
     */
    public final void setX(double x) {

        this.x = x;
    }

    /**
     * Get Y
     * 
     * @return the y
     */
    public final double getY() {

        return y;
    }

    /**
     * Set Y
     * 
     * @param y the y coordinate to set
     */
    public final void setY(double y) {

        this.y = y;
    }

    /**
     * Get Z
     * 
     * @return the z
     */
    public final double getZ() {

        return z;
    }

    /**
     * Set Z
     * 
     * @param z the z coordinate to set
     */
    public final void setZ(double z) {

        this.z = z;
    }

    /**
     * Get Node name
     * 
     * @return String
     */
    public String getName() {

        return name;
    }

    /**
     * Set node name
     * 
     * @param String
     */
    public void setName(String name) {

        this.name = name;
    }

    @Override
    public String toString() {

        return "Node #" + id + " " + name + " (" + x + ", " + y + ", " + z + ")";
    }

    /**
     * If node is enabled
     * 
     * @return the enabled
     */
    public boolean isEnabled() {

        return enabled;
    }

    /**
     * Set node enabled/disabled
     * 
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

}
