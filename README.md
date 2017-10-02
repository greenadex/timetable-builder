# timetable-builder

Command Line Application for generating a timetable-calendar in Google Calendar based on the timetable given by the Faculty of Mathematics and Computer Science of "Babes-Bolyai" University.

| **Turns this ([cs.ubbcluj.ro](http://www.cs.ubbcluj.ro/files/orar/2017-1/tabelar/index.html))** | ![alt text](https://i.imgur.com/rsytfGp.png "Before") |
|:-----------------------------------------------------------------------------------------------:|:-----------------------------------------------------:|
|                                                                                                 | ⬇️                                                     |
| **Into this ([calendar.google.com](https://calendar.google.com))**                              | ![alt text](https://i.imgur.com/2E89kIE.png "After")  |

## Prerequisites

- Java 1.8+
- [Gradle](https://gradle.org/install)

## Usage

```bash
$ gradle -q run
```

And then follow the on-screen instructions. If you want only a subset of the activities in the timetable, add the name of the activites that you want to participate to in `filtered_activities.txt` and run the app with:

```bash
$ gradle -q run -PappArgs="['filter']"
```
