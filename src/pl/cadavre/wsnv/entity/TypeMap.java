
package pl.cadavre.wsnv.entity;

import java.util.HashMap;

import pl.cadavre.wsnv.type.Type;

public class TypeMap extends HashMap<Integer, Type> {

    public Type[] getAvailableTypes() {

        Type[] types = new Type[this.size()];
        this.values().toArray(types);

        return types;
    }

    public Integer[] getColumns() {

        Integer[] indexes = new Integer[this.size()];
        this.keySet().toArray(indexes);

        return indexes;
    }

    public Type getTypeForColumn(int index) {

        return this.get(index);
    }

    public int getColumnForType(Type type) {

        int index = 0;
        for (Type availableType : getAvailableTypes()) {
            if (availableType.getClass().getName() == type.getClass().getName()) {
                break;
            }
            index++;
        }

        return getColumns()[index];
    }

}
