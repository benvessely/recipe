# Nutrition Calculator
Created by Ben Vessely, who can be contacted at benvessely at gmail dot com. 

## Description 
This is a Java Spring Boot project with a RESTful API that can be used to calculate the nutrition information of a recipe. Since this first version of the project lacks a frontend component, the project is made up of a series of API calls that take on different responsibilities in the workflow. The data being used for nutrition information is the SR Legacy dataset as provided by the USDA.

My base idea for this project was just to perform CRUD operations on recipes, with the recipes being stored in a PostgreSQL database, and this functionality is still intact. Once a recipe is in the database, there is an `ingredient-matches` endpoint that can be used to fetch the top 15 most relevant matches from the ingredient database, with relevance determined by a multi-part matching algorithm. The user then picks the database ingredient that most closely matches what the user was searching for for each original ingredient (Note: we differentiate between "original ingredient", which is what the user gives in the recipe, and "database ingredient", which is an element from the food database; in this document, the difference between original and database ingredients will sometimes be implied via context). 

Once the selection of database ingredients has been made, these choices are passed into the `portions` endpoint. This endpoint takes care of finding all possible portion data for each chosen database ingredient. This portion data is needed because the nutritional information is stored in units per gram in the database, so for user inputs like "1 cup flour" and "4 carrots", we need a way to convert those units to grams. 

These portion options are then returned to the user, who chooses the most relevant ones for their recipe (for example, the portions for the previous examples might be 1 cup of flour is 50g, and a medium-sized carrot is 25g). The user then passes the chosen portions the quantity of those portions into the `calculate-nutrition` endpoint. This endpoint then calculates the final recipe nutrition and returns it to the user.

## Installation and Setup 


The first step is to clone the project onto your machine, which can be done by running
```bash
git clone https://github.com/benvessely/recipe.git
```
in the command line from the root directory of your filesystem (or any other desired location). This should create a copy of the repository called `recipe` containing the repository from git. 

Then, there are some CSV files that are required by the function. They are too big to store using git, but they can be downloaded by visiting  [this link](https://fdc.nal.usda.gov/download-datasets), scrolling down to the Latest Downloads section, then in the row for the "SR Legacy" Data Type, click to download the file that says "April 2018 (CSV)". This should download the data needed for the project, which should not change, as this database is no longer updated as of 2018. Once you have the data downloaded, unzip it, rename the unzipped folder `nutrition_data`, and move this folder to the `src/main/resources/` directory within your `recipe` project. 

One important thing to note is that this data is still relevant, even if it hasn't been updated in some years, as the nutrition information for basic ingredients, like the ingredients in this database, have also not changed. 

From here, the instructions vary based on operating system. For all platforms, you need Postgres version 12 or newer and Java version 17. 

#### Mac
1. Download the OpenJDK 17 version of Java, which can be done via homebrew with
```bash
brew install openjdk@17
```
2. Download Postgres via the installer at [this link](https://www.postgresql.org/download/macosx/), or via homebrew with 
```bash
brew install postgresql
```
3. Create the database:
```bash
psql postgres -c "CREATE DATABASE recipe_app;"
```
4. Create new user `recipe_user` with a password:
```bash
psql postgres -c "CREATE USER recipe_user WITH PASSWORD <your_secure_password>;"
```
replacing `<your_secure_password>` with a password of your choice.
5. Create or modify `src/main/resources/application.properties` with the following settings:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/recipe_app
spring.datasource.username=recipe_user
spring.datasource.password=<your_secure_password>
```
replacing `<your_secure_password>` with the password you chose previously.

 
#### Windows

1. Download the version of OpenJDK 17 that fits your computer architecture from this site: https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-17. 

2. Follow the instructions from this site to get Postgres working using the interactive installer: https://www.postgresql.org/download/windows/.

3. We need to create the database that will be used in the project, so from the command line, run 
```bash
psql -U postgres -c "CREATE DATABASE recipe_app;"
```

4. Create a new user that we will use to interact with the database: 
```bash
psql postgres -c "CREATE USER recipe_user WITH PASSWORD <your_secure_password>;"
```

5. Create or modify the file `src/main/resources/application.properties` with the following settings:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/recipe_app
spring.datasource.username=postgres
spring.datasource.password=<your_secure_password>
```
replacing `<your_secure_password>` with the password you chose while setting up Postgres earlier. 



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


## Installation and Setup Testing

Here is some documentation about how I tested the above installation methods. For Linux, I used a containerized approach to simulate a fresh Linux environment. For Mac and Windows, it seemed like an unnecessarily complex challenge to emulate those environments for some relatively simple installation commands that I verified in the psql documentation, so I have no in depth installation/setup testing for those operating systems.  

#### Linux

The Dockerfile and shell testing script `setup-and-test.sh` that I used to simulate the steps in the Installation and Setup section are within the `/InstallTesting/Linux` subdirectory of the project root directory. I copied the relevant food data CSV files into a subdirectory of `/InstallTesting/Linux` so that the files would be available in the build context when we run the build command below. The Dockerfile, and thereby the test script, can be used to build an image using
```bash
docker build -t recipe-linux-test .
```
and then the container can be run using
```bash
docker run -it recipe-linux-test
```
After executing these commands, which follow my installation steps above for setting up the environment and successfully running my unit and integration tests within the container, I can conclude that the installation and setup steps for Linux are sound.  


## API Usage
Spring Boot applications run, by default, on `localhost:8080`, though this can be changed by setting `server.port=x` in the application.properties file to use port `x`. It will henceforth be assumed that all API URLs should be prefaced with `localhost:8080` to be accessed successfully. 

### Endpoints

#### Create Recipe
This endpoint creates recipe in the database given recipe information in the form of JSON data. 

URL: `/api/recipes`

Method: PUT

Request Body: JSON data with the following structure. I will give the JSON types, but note that these are automatically mapped to Java types when this data is converted into a RecipeModel in RecipeController.java.

| Field     | Type      | Required | Description                   |
|-----------|-----------|----------|-------------------------------|
| title     | string    | Yes      | Name of the recipe            |
| description | string  | No       | Brief description of the recipe |
| ingredients | string  | Yes      | Ingredients with quantity and units, separated by newlines |
| instructions| string  | Yes      | Recipe instructions, separated by newline | 
| servings    | number (converted to Integer) | No | Number of servings |
| prepTimeMinutes | number (converted to Integer) | No | Minutes of prep time|
| cookTimeMinutes | number (converted to Integer) | No | Minutes of cook time| 

Example:
```json
{
"title": "Sugar Cookies",
"description": "Yummy, homemade sugar cookies",
"ingredients": "1/3 cup sugar\n2 eggs\n0.5 oz butter\n1 cup brown sugar",
"instructions": "1. Mix ingredients\n2. Bake at 375°F for 10 minutes",
"servings": 36,
"prepTimeInMinutes": 20,
"cookTimeMinutes": 15
}
```

Response Body: I again give the JSON types, but I give what it was converted from in Java if relevant. Note that after this endpoint creates the recipe in the database, it then immediately queries for and returns the recipe from the database.

| Field     | Type      | Required | Description                   |
|-----------|-----------|----------|-------------------------------|
| id        | number (converted from Integer) | Yes | Id created by Postgres|
| title     | string    | Yes      | Name of the recipe            |
| description | string  | No       | Brief description of the recipe |
| ingredients | string  | Yes      | Ingredients with quantity and units, separated by newlines |
| instructions| string  | Yes      | Recipe instructions, separated by newline |
| servings    | number (converted from Integer) | No | Number of servings |
| prepTimeMinutes | number (converted to Integer) | No | Minutes of prep time|
| cookTimeMinutes | number (converted to Integer) | No | Minutes of cook time| 

Example: 

```json
{
    "id": 33,
    "title": "Sugar Cookies",
    "description": "Yummy, homemade sugar cookies",
    "ingredients": "1/3 cup sugar\n2 eggs\n0.5 oz butter\n1 cup brown sugar",
    "instructions": "1. Mix ingredients\n2. Bake at 375°F for 10 minutes",
    "servings": 36,
    "prepTimeMinutes": 20,  
    "cookTimeMinutes": 15,
    "createdAt": "2025-04-07T13:33:41.065427",
    "updatedAt": null
}
```

#### Get All Recipes
This endpoint fetches all recipes from the database and returns them in the response body as a JSON array. 

URL: `/api/recipes`

Method: GET 

Request Body: N/A

Response Body:
```json
[
    {
        "id": 2,
        "title": "Seared Steak",
        "description": "Buttery, garlicky steak",
        "ingredients": "2 pounds sirloin steak\n1/4 cup butter\n1 g garlic",
        "instructions": "1. Heat grill over high heat\n2. Add butter, then cook steaks for 3 minutes\n3. Flip steaks, add garlic",
        "servings": 6,
        "prepTimeMinutes": 0,
        "cookTimeMinutes": 15,
        "createdAt": "2025-04-07T14:15:24.728843",
        "updatedAt": null
    },
    {
        "id": 1,
        "title": "Sugar Cookies",
        "description": "Yummy, homemade sugar cookies",
        "ingredients": "1/3 cup sugar\n2 eggs\n0.5 oz butter\n1 cup brown sugar",
        "instructions": "1. Mix ingredients\n2. Bake at 375°F for 10 minutes",
        "servings": 36,
        "prepTimeMinutes": 20,
        "cookTimeMinutes": 15,
        "createdAt": "2025-04-07T13:33:41.065427",
        "updatedAt": null
    }
]
```

#### Get Recipe by Id
Fetch a recipe from the database by its id.

URL: `/api/recipes/{id}`, where `id` is an integer corresponding to one of the recipe id in the database.

Method: GET

Example use: /api/recipes/2

Request Body: N/A

Response Body: 
```json
{
    "id": 2,
    "title": "Seared Steak",
    "description": "Buttery, garlicky steak",
    "ingredients": "2 pounds sirloin steak\n1/4 cup butter\n1 g garlic",
    "instructions": "1. Heat grill over high heat\n2. Add butter, then cook steaks for 3 minutes\n3. Flip steaks, add garlic",
    "servings": 6,
    "prepTimeMinutes": 0,
    "cookTimeMinutes": 15,
    "createdAt": "2025-04-07T14:15:24.728843",
    "updatedAt": null
}
```

#### Update Recipe







