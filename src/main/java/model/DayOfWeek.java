package model;

import model.exception.InvalidParameterException;

public enum DayOfWeek {
    Monday(0), Tuesday(1), Wednesday(2), Thursday(3), Friday(4), Saturday(5), Sunday(6);

    static DayOfWeek get(String day) {
        switch (day) {
            case "Luni":
                return Monday;
            case "Marti":
                return Tuesday;
            case "Miercuri":
                return Wednesday;
            case "Joi":
                return Thursday;
            case "Vineri":
                return Friday;
            case "Sambata":
                return Saturday;
            case "Duminica":
                return Sunday;
            default:
                throw new InvalidParameterException(day + " is not a valid day of the week!");
        }
    }

    public final int index;

    DayOfWeek(int index) {
        this.index = index;
    }
}
