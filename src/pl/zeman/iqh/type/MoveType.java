
package pl.zeman.iqh.type;

/**
 * Move type Class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public final class MoveType extends RawType {

    public final String name = "Move";

    public static final int UNKNOWN = 0;

    public static final int MOVE_NONE = 1;

    public static final int MOVE_PRESENT = 2;

    /**
     * Get move status
     * 
     * @param reading
     * @return Integer
     */
    public int getStatus(int reading) {

        if (reading < 10) {
            return MOVE_NONE;
        } else if (reading > 700) {
            return MOVE_PRESENT;
        } else {
            return UNKNOWN;
        }
    }

}
