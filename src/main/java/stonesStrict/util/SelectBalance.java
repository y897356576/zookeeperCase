package stonesStrict.util;

import java.util.List;
import java.util.Random;

public class SelectBalance {

    private static final Random random = new Random();

    public static Object selectBalance(List list) {
        if(list == null || list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    public static Object selectBalance(Object[] array) {
        if(array == null || array.length == 0) {
            return null;
        }
        return array[random.nextInt(array.length)];
    }

}
