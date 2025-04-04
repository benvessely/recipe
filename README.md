# Nutrition Calculator
Created by Ben Vessely, who can be contacted at benvessely at gmail dot com. 

## Description 
This is a Java Spring Boot project with a RESTful API that can be used to calculate the nutrition information of a recipe. Since this first version of the project lacks a frontend component, the project is made up of a series of API calls that take on different responsibilities in the workflow. The data being used for nutrition information is from GET THIS. 

My base idea for this project was just to perform CRUD operations on recipes, with the recipes being stored in a PostgreSQL database, and this functionality is still intact. Once a recipe is in the database, there is an `ingredient-matches` endpoint that can be used to fetch the top 15 most relevant matches from the ingredient database, with relevance determined by a multi-part matching algorithm. The user then picks the database ingredient that most closely matches what the user was searching for for each original ingredient (Note: we differentiate between "original ingredient", which is what the user gives in the recipe, and "database ingredient", which is an element from the food database; in this document, the difference between original and database ingredients will sometimes be implied via context). 

Once the selection of database ingredients has been made, these choices are passed into the `portions` endpoint. This endpoint takes care of finding all possible portion data for each chosen database ingredient. This portion data is needed because the nutritional information is stored in units per gram in the database, so for user inputs like "1 cup flour" and "4 carrots", we need a way to convert those units to grams. 

These portion options are then returned to the user, who chooses the most relevant ones for their recipe (for example, the portions for the previous examples might be 1 cup of flour is 50g, and a medium-sized carrot is 25g). The user then passes the chosen portions the quantity of those portions into the `calculate-nutrition` endpoint. This endpoint then calculates the final recipe nutrition and returns it to the user.

## Installation


### Prerequisites
These requirements must be downloaded locally on your machine for the application to work. 
- Java 17 or higher
- PostgreSQL 12 or higher
I will include steps below on how specifically to do this for each operating system below.

### Setup instructions
The first step is to clone the project onto your machine, which can be done by running
```bash
git clone https://github.com/benvessely/recipe.git
```
in the terminal/command line from the root directory of your filesystem (or any other desired location). 

From here, the instructions vary based on operating system. 

#### Mac
1. If you don't already have Java and Postgres downloaded, then from the command line run
```bash
brew install openjdk@17
brew install postgresql@14
```
2. Start the Postgres service:
```bash 
brew services start postgresql@14
```
3. Create the database:
```bash
psql postgres -c "CREATE DATABASE recipe_app;"
```
4. Create new user `recipe_user` with a password:  
```bash
psql postgres -c "CREATE USER recipe_user WITH PASSWORD <your_secure_password>;"
```
replacing <your_secure_password> with a password of your choice. 






#### Linux

1. If you don't already have Java and Postgres downloaded, then from the command line run 
```bash
apt-get update
apt-get install -y openjdk-17-jdk postgresql postgresql-contrib 
```
2. Get the Postgres service started using the command line:
```bash
service postgresql start
```
3. Create a database named `recipe_app` using the Postgres interactive terminal `psql` and the default user `postgres`.  
```bash
sudo -u postgres psql -c "CREATE DATABASE recipe_app;"
```
Note that the schema.sql file will be run automatically to create the necessary tables when the application is run due to the `@EventListener(ApplicationReadyEvent.class)` in the DataLoaderService file. 
4. Set a password using 
```bash
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD <your_postgres_password>;"
```
replacing `<your_postgres_password>` with a chosen password. 
5. Create or modify `src/main/resources/application.properties` with the following settings:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/recipe_app
spring.datasource.username=postgres
spring.datasource.password=<your_postgres_password>
```
replacing `<your_postgres_password>` with the password you chose in the last step. 




## Running the application

The application can be run via the command line command `./mvnw spring-boot:run`from the project root. This gets the API running, for which the usage details are included later in this document. 

The tests can be run via `./mvnw test`, again from the project root. 


## Testing of Installation

Here is some documentation about how I tested the above installation methods using Docker to simulate different operating systems.

#### Linux

The Dockerfile and shell testing script `setup-and-test.sh` that I used to simulate the steps in the Installation section are within the `/InstallTesting/Linux` subdirectory of the project root directory. I copied the relevant food data CSV files into a subdirectory of `/InstallationTesting/Linux` so that the files would be available in the build context when we run the build command below. The Dockerfile, and thereby the test script, can be used to build an image using
```bash
docker build -t recipe-linux-test .
```
and then the container can be run using
```bash
docker run -it recipe-linux-test
```
After executing these commands, which follow my installation steps above and then run my unit and integration tests within the container, I can conclude that the installation is sound.  