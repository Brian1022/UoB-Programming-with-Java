# UoB-Programming-with-Java
# [cite_start]UoB-Programming-with-Java [cite: 1]

This repository contains two major Java projects developed during my MSc in Computer Science at the University of Bristol. These projects demonstrate my proficiency in Object-Oriented Programming (OOP), Client-Server architecture, custom parsing, and state management.

## 📂 Project Overview

### 1. Custom Relational Database Engine (`cw-db`)
[cite_start]A lightweight, file-based SQL-like database management system built entirely from scratch in Java, operating over a networked client-server architecture[cite: 18, 19].

* [cite_start]**Client-Server Communication:** Implemented socket programming allowing multiple clients to send queries to a central `DBServer` listening on port 8888[cite: 18, 19].
* [cite_start]**Query Parsing & Execution:** Developed a robust command interpreter capable of handling standard SQL operations including `CREATE`, `INSERT`, `UPDATE`, `DELETE`, `SELECT`, `ALTER`, and `DROP`[cite: 19]. 
* [cite_start]**Advanced Condition Evaluation:** Engineered a custom parser that converts infix `WHERE` conditions into Postfix (Reverse Polish Notation) using a Stack to accurately evaluate complex boolean logic (e.g., `>`, `<`, `==`, `LIKE`, `AND`, `OR`)[cite: 21].
* [cite_start]**Table Joins:** Implemented relational `JOIN` operations to merge multiple tables dynamically based on matching attributes[cite: 27].
* [cite_start]**Persistent File Storage:** Designed a structured file I/O system where databases are stored as directories and tables are maintained as tab-separated (`.tab`) files, ensuring data persistence across server restarts[cite: 24, 25, 62].

### 2. Multiplayer Text Adventure Game Engine (`cw-stag`)
[cite_start]A networked Multiplayer User Dungeon (MUD) text-based adventure game engine (STAG)[cite: 50, 58]. The game world and its logic are dynamically generated from configuration files, meaning the engine can run entirely different games without changing the underlying Java code.

* [cite_start]**Dynamic World Generation:** * Integrated the `alexmerz` graphviz parser to read `.dot` files, dynamically generating game locations, paths, artefacts, furniture, and characters (`GameEntityParser.java`)[cite: 53].
  * [cite_start]Utilized Java's DOM XML parser to load complex, custom game actions and state transitions from `.xml` files (`GameActionParser.java`)[cite: 47, 58].
* [cite_start]**Command Validation & Execution:** Built a sophisticated Natural Language processing validator that handles both built-in commands (`look`, `inv`, `get`, `drop`, `goto`) and dynamically loaded custom actions (e.g., `open`, `chop`, `drink`) while resolving ambiguities and extraneous entities[cite: 43, 44].
* [cite_start]**Multiplayer State Management:** Developed a `GamePlayerState` architecture to track multiple concurrent players, their individual inventories, current health points, and locations within the game world[cite: 45, 57].
* [cite_start]**Event-Driven Mechanics:** Implemented robust game mechanics handling entity consumption, production, and player death/respawn scenarios based on triggered actions[cite: 45].

## 🛠️ Key Technical Skills Demonstrated
* [cite_start]**Core Java:** Interfaces, Abstract Classes (`GameEntity`), Collections Framework (HashMaps, HashSets, Lists), and Stream APIs[cite: 21, 52, 59].
* [cite_start]**Architecture:** Client-Server Socket Programming (TCP/IP), MVC-like command delegation[cite: 18, 19].
* [cite_start]**Data Processing:** Abstract Syntax Trees / RPN for logic evaluation, File I/O (BufferedReader/Writer), XML DOM Parsing, Graphviz DOT parsing[cite: 21, 24, 47, 53].
* [cite_start]**Testing:** Extensive automated unit and integration testing using JUnit 5[cite: 62].
