# Nutrition Calculator
Created by Ben Vessely, who can be contacted at benvessely at gmail dot com. 

## Description 
This is a Java Spring Boot project with a RESTful API that can be used to calculate the nutrition information of a recipe. Since this first version of the project lacks a frontend component, the project is made up of a series of API calls that take on different responsibilities in the workflow. The data being used for nutrition information is from GET THIS. 

My base idea for this project was just to perform CRUD operations on recipes, with the recipes being stored in a PostgreSQL database, and this functionality is still intact. Once a recipe is in the database, there is an `ingredient-matches` endpoint that can be used to fetch the top 15 most relevant matches from the ingredient database, with relevance determined by a multi-part matching algorithm. The user then picks the database ingredient that most closely matches what the user was searching for for each original ingredient (Note: we differentiate between "original ingredient", which is what the user gives in the recipe, and "database ingredient", which is an element from the food database; in this document, the difference between original and database ingredients will sometimes be implied via context). 

Once the selection of database ingredients has been made, these choices are passed into the `portions` endpoint. This endpoint takes care of finding all possible portion data for each chosen database ingredient. This portion data is needed because the nutritional information is stored in units per gram in the database, so for user inputs like "1 cup flour" and "4 carrots", we need a way to convert those units to grams. 

These portion options are then returned to the user, who chooses the most relevant ones for their recipe (for example, the portions for the previous examples might be 1 cup of flour is 50g, and a medium-sized carrot is 25g). The user then passes the chosen portions the quantity of those portions into the `calculate-nutrition` endpoint. This endpoint then calculates the final recipe nutrition and returns it to the user.

## Installation

The first step is to clone the project onto the user's machine, which can be done by running 
```commandline
git clone https://github.com/benvessely/recipe.git
```
in the terminal/command line.

### Prerequisites
- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

### Database Setup
1. Install PostgreSQL:
    - **macOS**: Install via
      ```bash 
      brew install postgresql
      brew services start postgresql
      ```
      or download from [postgresql.org](https://www.postgresql.org/download/macosx/)
    - **Windows**: Download the installer from [postgresql.org](https://www.postgresql.org/download/windows/) 
    - **Linux**: Use your package manager, e.g., `sudo apt install postgresql`

2. Create a database. The first line uses the Postgres interactive terminal interface `psql` to create a new user named `postgres`, the second line creates the actual database, and the third line quits `psql`, 
    ```bash
    psql -U postgres
    CREATE DATABASE recipe_app
    \q
    ```
Note that the schema.sql file will be run automatically when the application is run due to the `@EventListener(ApplicationReadyEvent.class)` in the DataLoaderService file, which will create the necessary tables for the application. 

3. Configure Database Connection

Create or modify `src/main/resources/application.properties` with the following settings:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/recipe_app
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password
```
replacing your_postgres_password with the password you set for the postgres user (if you set one). 



