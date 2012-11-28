
package pl.cadavre.wsnv.type;

/**
 * Factory for reading Types
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class TypeFactory {

    public static Type get(String name) {

        if (name.equals("temp")) {
            return new TemperatureType();
        } else if (name.equals("light")) {
            return new LightType();
        } else if (name.equals("voltage")) {
            return new Type();
        } else {
            return null;
        }
    }

}
