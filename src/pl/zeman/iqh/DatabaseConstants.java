
package pl.zeman.iqh;

/**
 * Constant Class with Database fields listing
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class DatabaseConstants {

    public static final String HEALTH_TABLE = "node_health";

    public static final String RESULTS_TABLE = "xbw_da100_results";

    public final class Health {

        public static final String ID = "nodeid";

        public static final String PARENT_ID = "parent";

        public static final String TIMESTAMP = "result_time";

        public static final String HEALTH_PACKETS_COUNT = "health_pkts";

        public static final String NODE_PACKETS_COUNT = "node_pkts";

        public static final String FORWARDED_PACKETS_COUNT = "forwarded";

        public static final String DROPPED_PACKETS_COUNT = "dropped";

        public static final String RETRIES_COUNT = "retries";

        public static final String BATTERY = "battery";

        public static final String TX_QUALITY = "quality_tx";

        public static final String RX_QUALITY = "quality_rx";

        public static final String PARENT_RSSI = "parent_rssi";

    }

    public final class Results {

        public static final String ID = "nodeid";

        public static final String PARENT_ID = "parent";

        public static final String TIMESTAMP = "result_time";

        public static final String VOLTAGE = "voltage";

        public static final String TEMPERATURE = "temp";

        public static final String LIGHT = "light";

        public static final String MOVE = "adc2";

    }

}
