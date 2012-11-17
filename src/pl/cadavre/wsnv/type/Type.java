
package pl.cadavre.wsnv.type;

/**
 * Type representation base Class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public abstract class Type {

    static String name = "Raw";

    static String unit = "";

    static String unitShort = "";

    static double factor = 1;

    static double adding = 0;

    public static double convert(int reading) {

        return reading * factor + adding;
    }

    public static boolean isMultiunit() {

        return false;
    }

}
