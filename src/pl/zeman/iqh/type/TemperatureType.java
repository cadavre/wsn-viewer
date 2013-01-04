
package pl.zeman.iqh.type;

/**
 * Temperature type Class
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public final class TemperatureType extends RawType {

    public final String name = "Temperature";

    public String unit = "kelvin";

    public String unitShort = "K";

    double factor = 1;

    double adding = 0;

    /**
     * Convert reading to readable format
     */
    public double convert(int reading) {

        double Rthr = 10000.0 * (1023 - reading) / reading;
        double calculation = 0.001010024 + (0.000242127 * Math.log(Rthr))
                + (0.000000146 * Math.pow(Math.log(Rthr), 3));
        double result = (1 / calculation);

        return result * factor - adding;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMultiunit() {

        return true;
    }

    /**
     * Set Kelvin format
     */
    public void setKelvin() {

        unit = "kelvin";
        unitShort = "K";
        factor = 1;
        adding = 0;
    }

    /**
     * Set Celsius format
     */
    public void setCelsius() {

        unit = "degree Celsius";
        unitShort = "\u00B0C";
        factor = 1;
        adding = 273.15;
    }

    /**
     * Set Farenthite format
     */
    public void setFarenthite() {

        unit = "degree Fahrenheit";
        unitShort = "\u00B0FF";
        factor = 9.0 / 5.0;
        adding = -459.67;
    }

}
