package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 11.09.2016.
 */

class Day {
    /**
     * List of all the Classes of the current Day
     */
    private List<Class> classes = new ArrayList<>();

    /**
     * Adds a new Class to the current Day
     * @param newClass - the new class of the day
     */
    void addClass(Class newClass) {
        if (newClass == null) {
            throw new NullPointerException("Tried to add null class!");
        }

        classes.add(newClass);
    }

    List<Class> getClasses() {
        return classes;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (Class nextClass : classes) {
            output.append(nextClass).append("\n");
        }
        return output.toString();
    }
}
