#!/bin/bash

target="build/libs/timetable-builder-1.0.jar"

if [[ ! -e ${target} ]]; then
  gradle clean
  gradle build
  gradle fatJar
fi

java -jar ${target} $@
