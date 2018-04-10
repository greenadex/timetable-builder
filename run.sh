gradle clean
gradle build
gradle fatJar
java -jar build/libs/timetable-builder-1.0.jar $@
