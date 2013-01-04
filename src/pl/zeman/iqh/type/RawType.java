
package pl.zeman.iqh.type;

/**
 * Type representation base Class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class RawType {

    public String name = "Raw";

    public String unit = "";

    public String unitShort = "";

    double factor = 1;

    double adding = 0;

    /**
     * Convert type value with preset factor and adding
     * 
     * @param reading
     * @return Double
     */
    public double convert(int reading) {

        return reading * factor + adding;
    }

    /**
     * Check if factor and adding can be pre set
     * 
     * @return Boolean
     */
    public boolean isMultiunit() {

        return false;
    }

}
