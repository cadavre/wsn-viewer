
package pl.cadavre.wsnv.entity;

import java.util.HashMap;

import pl.cadavre.wsnv.type.RawType;

/**
 * Mapping of database types
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class TypeMap extends HashMap<Integer, RawType> {

    public RawType[] getAvailableTypes() {

        RawType[] types = new RawType[this.size()];
        this.values().toArray(types);

        return types;
    }

    public Integer[] getColumns() {

        Integer[] indexes = new Integer[this.size()];
        this.keySet().toArray(indexes);

        return indexes;
    }

    public RawType getTypeForColumn(int index) {

        return this.get(index);
    }

    public int getColumnForType(RawType type) {

        int index = 0;
        for (RawType availableType : getAvailableTypes()) {
            if (availableType.getClass().getName() == type.getClass().getName()) {
                break;
            }
            index++;
        }

        return getColumns()[index];
    }

}
