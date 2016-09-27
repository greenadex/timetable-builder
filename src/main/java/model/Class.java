package model;

/**
 * Created on 11.09.2016.
 */
public class Class {
    /**
     * The necessary information of a class
     */
    private String day;
    private String period;
    private String startingHour;
    private String endingHour;
    private String frequency;
    private String classRoom;
    private String group;
    private String typeOfClass;
    private String nameOfClass;
    private String professor;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
        String[] hours = period.split("-");
        startingHour = (hours[0].length() == 1 ? "0" : "") +  hours[0] + ":00";
        endingHour = (hours[1].length() == 1 ? "0" : "") +  hours[1] + ":00";
    }

    public String getStartingHour() {
        return startingHour;
    }

    public String getEndingHour() {
        return endingHour;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        //In the HTML source code, no frequency is signaled by using '&nbsp;'; therefore, we map all these occurences
        //to an empty String
        if (frequency.equals("&nbsp;")) {
            this.frequency = "";
        } else {
            this.frequency = frequency;
        }
    }

    public String getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(String classRoom) {
        this.classRoom = classRoom;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTypeOfClass() {
        return typeOfClass;
    }

    public void setTypeOfClass(String typeOfClass) {
        this.typeOfClass = typeOfClass;
    }

    public String getNameOfClass() {
        return nameOfClass;
    }

    public void setNameOfClass(String nameOfClass) {
        this.nameOfClass = nameOfClass;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    /**
     * Sets the information based on a number. Each field has a theoretical index associated to id based
     * on the order of declaration. This function is used when parsing a class from the HTML source code.
     * @param number - the index of the field we wish to set
     * @param information - the information we wish to add to the current class
     */
    public void setInformation(int number, String information) {
        switch (number) {
            case 0:
                setDay(information);
                break;
            case 1:
                setPeriod(information);
                break;
            case 2:
                setFrequency(information);
                break;
            case 3:
                setClassRoom(information);
                break;
            case 4:
                setGroup(information);
                break;
            case 5:
                setTypeOfClass(information);
                break;
            case 6:
                setNameOfClass(information);
                break;
            case 7:
                setProfessor(information);
                break;
            default:
                throw new IllegalArgumentException("Error parsing class! Number is " + number);
        }
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s | %s | %s | %s", day, period, frequency, classRoom, group,
                typeOfClass, nameOfClass, professor);
    }
}