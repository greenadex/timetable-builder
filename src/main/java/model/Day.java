package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 11.09.2016.
 */
class Day {
    /**
     * List of all the Activities of the current Day
     */
    private List<Activity> activities = new ArrayList<>();

    /**
     * Adds a new Activity to the current Day
     *
     * @param newActivity - the new activity of the day
     */
    void addActivity(Activity newActivity) {
        if (newActivity == null) {
            throw new NullPointerException("Tried to add null activity!");
        }

        activities.add(newActivity);
    }

    List<Activity> getActivities() {
        return activities;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (Activity nextActivity : activities) {
            output.append(nextActivity).append("\n");
        }
        return output.toString();
    }
}
