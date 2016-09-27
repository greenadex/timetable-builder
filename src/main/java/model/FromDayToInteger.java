package model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 26.09.2016.
 */
public class FromDayToInteger {
    /**
     * Map which makes the connection between the name of a day in Romanian and its index a week
     */
    private static final Map<String, Integer> NAME_OF_DAYS = new HashMap<>();

    /*
      Fill the map with the necessary values
     */
    static {
        NAME_OF_DAYS.put("Luni", 0);
        NAME_OF_DAYS.put("Marti", 1);
        NAME_OF_DAYS.put("Miercuri", 2);
        NAME_OF_DAYS.put("Joi", 3);
        NAME_OF_DAYS.put("Vineri", 4);
        NAME_OF_DAYS.put("Sambata", 5);
        NAME_OF_DAYS.put("Duminica", 6);
    }


    public static int getInteger(String day) {
        return NAME_OF_DAYS.get(day);
    }
}
