
package pl.cadavre.wsnv.type;

/**
 * Light type Class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public final class LightType extends Type {

    public final String name = "Light";

    public static final int LEVEL_DARK = 0;

    public static final int LEVEL_DUSK = 0;

    public static final int LEVEL_BRIGHT = 2;

    public static final int LEVEL_SUNNY = 3;

    public static final int LEVEL_GLARINGLY = 4;

    public int getLevel(int reading) {

        if (reading < 20) {
            return LEVEL_DARK;
        } else if (reading > 20 && reading < 100) {
            return LEVEL_DUSK;
        } else if (reading > 100 && reading < 300) {
            return LEVEL_BRIGHT;
        } else if (reading > 300 && reading < 1100) {
            return LEVEL_SUNNY;
        } else {
            return LEVEL_GLARINGLY;
        }
    }

}
