package model;

import lombok.Getter;

import java.security.InvalidParameterException;

@Getter
public class Activity {
    public enum Frequency {
        Weekly, EveryOddWeek, EveryEvenWeek;

        static Frequency get(String info) {
            if (info.contains("1")) {
                return EveryOddWeek;
            }

            if (info.contains("2")) {
                return EveryEvenWeek;
            }

            return Weekly;
        }
    }

    public enum Type {
        Lecture, Seminar, Laboratory;

        static Type get(String info) {
            switch (info) {
                case "Curs":
                    return Lecture;
                case "Seminar":
                    return Seminar;
                case "Laborator":
                    return Laboratory;
                default:
                    throw new InvalidParameterException("Invalid activity type: " + info);
            }
        }
    }

    private DayOfWeek dayOfWeek;
    private String startTime;
    private String endTime;
    private Frequency frequency;
    private String room;
    private String group;
    private Type type;
    private String name;
    private String professor;

    boolean isForSemiGroup(String semiGroup) {
        return getSemiGroup().equals("0") || semiGroup.equals("0") || getSemiGroup().equals(semiGroup);
    }

    private String getSemiGroup() {
        if (group.contains("/")) {
            return group.substring(group.length() - 1);
        }
        return "*";
    }


    /**
     * //TODO replace integers with enum
     */
    void setInformation(int number, String information) {
        switch (number) {
            case 0:
                dayOfWeek = DayOfWeek.get(information);
                break;
            case 1:
                setStartAndEndTime(information);
                break;
            case 2:
                frequency = Frequency.get(information);
                break;
            case 3:
                room = information;
                break;
            case 4:
                group = information;
                break;
            case 5:
                type = Type.get(information);
                break;
            case 6:
                name = information;
                break;
            case 7:
                professor = information;
                break;
            default:
                throw new IllegalArgumentException("Error parsing activity! No such field: " + number);
        }
    }

    private void setStartAndEndTime(String period) {
        String[] hours = period.split("-");
        startTime = (hours[0].length() == 1 ? "0" : "") + hours[0] + ":00";
        endTime = (hours[1].length() == 1 ? "0" : "") + hours[1] + ":00";
    }

    @Override
    public String toString() {
        return String.format("%s | %s-%s | %s | %s | %s | %s | %s | %s",
                dayOfWeek, startTime, endTime, frequency, room, group, type, name, professor);
    }
}
