package model;

public class StudentInfo {
    public final int group;
    public final int semiGroup;
    public final String timetableLink;

    public StudentInfo(String timetableLink, int group, int semiGroup) {
        this.timetableLink = timetableLink;
        this.group = group;
        this.semiGroup = semiGroup;
    }

    public int getYearOfStudy() {
        return (group / 10) % 10;
    }
}
