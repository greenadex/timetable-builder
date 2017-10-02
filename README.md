# timetable-builder
Command Line Application for generating a timetable-calendar in Google Calendar based on the timetable given by the Faculty of Mathematics and Computer Science of "Babes-Bolyai" University.

**Prerequisites:**

1. Java 1.8+
2. [Gradle](https://gradle.org/install)

**Run**: gradle -q run

If you want only a subset of the activities in the timetable, add the name of the activites that you want to participate to in *filtered_activities.txt* and run the app with: gradle -q run -PappArgs="['filter']".
