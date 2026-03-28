# UoB-Programming-with-Java

This repository contains two major Java projects developed during my MSc in Computer Science at the University of Bristol. These projects demonstrate my proficiency in Object-Oriented Programming (OOP), Client-Server architecture, custom parsing, and state management.

## 📂 Project Overview

### 1. Custom Relational Database Engine (`cw-db`)
A lightweight, file-based SQL-like database management system built entirely from scratch in Java, operating over a networked client-server architecture.

* **Client-Server Communication:** Implemented socket programming allowing multiple clients to send queries to a central `DBServer` listening on port 8888.
* **Query Parsing & Execution:** Developed a robust command interpreter capable of handling standard SQL operations including `CREATE`, `INSERT`, `UPDATE`, `DELETE`, `SELECT`, `ALTER`, and `DROP`. 
* **Advanced Condition Evaluation:** Engineered a custom parser that converts infix `WHERE` conditions into Postfix (Reverse Polish Notation) using a Stack to accurately evaluate complex boolean logic (e.g., `>`, `<`, `==`, `LIKE`, `AND`, `OR`).
* **Table Joins:** Implemented relational `JOIN` operations to merge multiple tables dynamically based on matching attributes.
* **Persistent File Storage:** Designed a structured file I/O system where databases are stored as directories and tables are maintained as tab-separated (`.tab`) files, ensuring data persistence across server restarts.

### 2. Multiplayer Text Adventure Game Engine (`cw-stag`)
A networked Multiplayer User Dungeon (MUD) text-based adventure game engine (STAG). The game world and its logic are dynamically generated from configuration files, meaning the engine can run entirely different games without changing the underlying Java code.

* **Dynamic World Generation:** * Integrated the `alexmerz` graphviz parser to read `.dot` files, dynamically generating game locations, paths, artefacts, furniture, and characters (`GameEntityParser.java`).
  * Utilized Java's DOM XML parser to load complex, custom game actions and state transitions from `.xml` files (`GameActionParser.java`).
* **Command Validation & Execution:** Built a sophisticated Natural Language processing validator that handles both built-in commands (`look`, `inv`, `get`, `drop`, `goto`) and dynamically loaded custom actions (e.g., `open`, `chop`, `drink`) while resolving ambiguities and extraneous entities.
* **Multiplayer State Management:** Developed a `GamePlayerState` architecture to track multiple concurrent players, their individual inventories, current health points, and locations within the game world.
* **Event-Driven Mechanics:** Implemented robust game mechanics handling entity consumption, production, and player death/respawn scenarios based on triggered actions.

## 🛠️ Key Technical Skills Demonstrated
* **Core Java:** Interfaces, Abstract Classes (`GameEntity`), Collections Framework (HashMaps, HashSets, Lists), and Stream APIs.
* **Architecture:** Client-Server Socket Programming (TCP/IP), MVC-like command delegation.
* **Data Processing:** Abstract Syntax Trees / RPN for logic evaluation, File I/O (BufferedReader/Writer), XML DOM Parsing, Graphviz DOT parsing.
* **Testing:** Extensive automated unit and integration testing using JUnit 5.
