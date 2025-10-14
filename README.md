# Master Thesis

![isel-logo](Logo/ISEL-Logo.png)

Project Work: Tool for generating course schedules with constraints

Advisors: [Nuno Leite](https://github.com/nleite-isel) and [Artur Ferreira](https://github.com/arturj)

## Introduction

This project aims to provide a graphical desktop application that simplifies the creation of Timetables. Timetabling problems fall within the category of combinatorial optimization and are classified as NP-Complete. As such, this task can be extremely difficult and time consuming for large datasets.

The data used on this project must follow the [International Timetabling Competition 2019](https://www.itc2019.org/home) format. The only deviations from the format were the exclusion of students and the addition of teachers.

The algorithms used on the timetabling creation were two. The first algorithm is used on the creation of an initial timetable, which will then be optimized. Said algorithm follows the framework proposed by Tomáš Müller in his [Ph.D. Thesis](https://muller.unitime.org/phd-thesis.pdf). The second algorithm is used to optimize the initial solution and uses Simulated Annealing to do so. This approach is based on [Edon Gashi and Kadri Sylejmani's approach](https://github.com/edongashi/itc-2019). Every parameter can be changed in the graphical interface.

## Features

- [x] Instructions
- [x] Storing of configurations between executions
- [x] Graphical interface for timetable creation
- [x] Import data in ITC2019 format
- [x] Initial timetable generation using Tomáš Müller framework
- [x] Timetable optimization with Simulated Annealing
- [x] Multithreading on the generation tasks
- [x] Queueing of generation tasks
- [x] Export data to ITC format
- [x] Export timetable to ITC format
- [ ] Export timetable to CSV
- [ ] Export timetable to PDF
- [ ] Export timetable to PNG

## Setup the database

Docker is required to instantiate the database

To setup the database a .env file is used. For security purposes it should be created by user. The file should follow the following format.

```
POSTGRES_PASSWORD=insert password here
```

More variables may be defined to customize the PostgreSQL instance.

## Building the application

Java JDK (11 or higher) and Maven are required to build the application

To build go to the folder ./TimetableGenerator and execute the following command:

```
mvn package
```

After the previous command has completed two jar files were generated on the target folder. Execute the jar file with "-jar-with-dependencies" in its name. To execute a jar file simply double click it or run the command:

```
java -jar TimetableGenerator-1.0-jar-with-dependencies.jar
```