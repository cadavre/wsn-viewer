
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

    public double convert(int reading) {

        return reading * factor + adding;
    }

    public boolean isMultiunit() {

        return false;
    }

}
