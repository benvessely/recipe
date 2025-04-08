# Nutrition Calculator
Created by Ben Vessely, who can be contacted at benvessely at gmail dot com. 

## Description 
This is a Java Spring Boot project with a RESTful API that can be used to calculate the nutrition information of a recipe. Since this first version of the project lacks a frontend component, the project is made up of a series of API calls that take on different responsibilities in the workflow. Additionally, my base idea for this project was just to perform CRUD operations on recipes, with the recipes being stored in a PostgreSQL database, and this functionality is still intact.

To actually calculate the nutrition, the first step is to make a request to the API to get the 15 database ingredients that are most similar to each recipe ingredient (Note: we sometimes differentiate between "recipe/original ingredient", which is what the user gives in the recipe, and "database ingredient", which is an element from the food nutrition database). The next step in calculating the nutrition is to fetch portion data for each chosen database ingredient. This portion data is needed because the nutritional information is stored in units per gram in the database, so for user inputs like "1 cup flour" and "4 carrots", we need a way to convert those quantities to grams. In the final step, the user then passes the chosen portions along with the quantity of those portions into the API, after which the nutrition information is calculated and returned. 

## Installation and Setup 

The first step is to clone the project onto your machine, which can be done by running
```bash
git clone https://github.com/benvessely/recipe.git
```
in the command line from the root directory of your filesystem (or any other desired location). This should create a copy of the repository called `recipe` containing the repository from git. 

There are some CSV files that are required for the calculation steps to work. They are too big to store using git, but they should be downloaded by visiting  [this link](https://fdc.nal.usda.gov/download-datasets), scrolling down to the Latest Downloads section, then in the row for the "SR Legacy" Data Type, clicking to download the file that says "April 2018 (CSV)". This should download the data needed for the project. Once you have the data downloaded, unzip it, rename the unzipped folder `nutrition_data`, and move this folder to the `src/main/resources/` directory within your `recipe` project. 

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
Spring Boot applications run, by default, on `localhost:8080`, though this can be changed by setting `server.port=x` in the application.properties file to use port `x`. It will henceforth be assumed that all API URLs should be prefaced with `localhost:8080`. 

Another relevant note is that I will be referencing the API from the perspective of a user, which means that the request and response bodies will be pure JSON. But, as soon as the request is sent to the server, Java deserializes the JSON into some sort of Java object, and, likewise, once the necessary steps have been carried out on the server side, the data that needs to be returned to the client is serialized back into JSON. I sometimes reference the form of the data when it is in deserialized form on the server side, and other times refer to this data in JSON form, as it's seen by the user---I will do my best to clarify the perspective with which I am referencing the data at any time. 

### Basic Endpoints

#### Create Recipe
This endpoint creates a recipe in the database given recipe information from the user.

URL: `/api/recipes`

Method: PUT

Request Body: The user submits JSON data with the following structure, which is converted into a RecipeModel object on the server side. Within these objects, the title, ingredients, and instructions must all be non-null. 

Example Request:
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

Response Body: After this endpoint creates the recipe in the database, it then immediately queries for and returns the recipe from the database.

Example Response: 

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
This endpoint fetches all recipes from the database.

URL: `/api/recipes`

Method: GET 

Request Body: N/A

Response Body: The recipes are returned within a JSON array.

Example:
```json
[
    {
        "id": 2,
        "title": "Seared Steak",
        "description": "Buttery, garlicky steak",
        "ingredients": "2 pounds sirloin steak\n1/4 cup butter\n3 cloves garlic",
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

URL: `/api/recipes/{id}`, where `id` is an integer corresponding to one of the recipe ids in the database.

Method: GET

Example use: /api/recipes/2

Request Body: N/A

Response Body: Contains a single JSON object

Example: 
```json
{
    "id": 2,
    "title": "Seared Steak",
    "description": "Buttery, garlicky steak",
    "ingredients": "2 pounds sirloin steak\n1/4 cup butter\n3 cloves garlic",
    "instructions": "1. Heat grill over high heat\n2. Add butter, then cook steaks for 3 minutes\n3. Flip steaks, add garlic",
    "servings": 6,
    "prepTimeMinutes": 0,
    "cookTimeMinutes": 15,
    "createdAt": "2025-04-07T14:15:24.728843",
    "updatedAt": null
}
```

#### Update Recipe
Update a recipe that's already in the database

URL: `/api/recipes/{id}`, where `id` is an integer corresponding to one of the recipe ids in the database.

Method: PUT 

Example use: /api/recipes/2

Request Body: Contains JSON in form of a Java RecipeModel object

Example: 
```json
{
    "title": "Light Sugar Cookies",
    "description": "Yummy, homemade sugar cookies, without butter",
    "ingredients": "1/3 cup sugar\n2 egg\n1 cup brown sugar",
    "instructions": "1. Mix ingredients\n2. Bake at 375°F for 10 minutes",
    "servings": 36,
    "prepTimeInMinutes": 20,
    "cookTimeMinutes": 15
}
```

Response Body: The updated recipe is immediately queried from the database and returned in the response body. 

Example Response: 
```json 
{
    "id": 34,
    "title": "Light Sugar Cookies",
    "description": "Yummy, homemade sugar cookies, without butter",
    "ingredients": "1/3 cup sugar\n2 egg\n1 cup brown sugar",
    "instructions": "1. Mix ingredients\n2. Bake at 375°F for 10 minutes",
    "servings": 36,
    "prepTimeMinutes": 20,
    "cookTimeMinutes": 15,
    "createdAt": "2025-04-07T13:33:41.065427",
    "updatedAt": "2025-04-07T14:36:27.721889"
}
```

### Nutrition Calculation Endpoints

#### Fetch Matches From Database
Given a recipe in the recipe table that's identified by its id, this endpoint fetches the 15 closest matches from the ingredients table for each ingredient in the recipe. This is the first step in calculating the nutrition information of a recipe: choosing ingredients from the database so that we can use the database's nutrition information for calculations regarding those ingredients. 

URL: `/api/recipes/{id}/ingredient-matches`, where `id` is an integer corresponding to one of the recipe ids in the database.

Method: GET

Example use: /api/recipes/2/ingredient-matches

Request Body: N/A

Response Body: Returns a JSON object that maps each ingredient name as a string to an array of JSON objects that were IngredientMatchModel objects in Java, sorted in decreasing order by the confidence of the match. 

The attributes of the IngredientMatchModel are as follows: `fdcId` is an integer called the FDC ID that uniquely identifies the ingredient from the ingredients table; `name` is the name of the ingredient in the table; `confidence` is a score between 0 and 1 that represents the confidence the algorithm has that this database ingredient is a correct match to the given recipe ingredient. 

Example Response: 
```json
{
    "garlic": [
        {
            "fdcId": 169230,
            "name": "Garlic, raw",
            "confidence": 0.8166666666666667
        },
        {
            "fdcId": 171325,
            "name": "Spices, garlic powder",
            "confidence": 0.30454545454545456
        },
        ...13 more items
    ],
    "butter": [
        {
            "fdcId": 173430,
            "name": "Butter, without salt",
            "confidence": 0.5
        },
        {
            "fdcId": 173411,
            "name": "Butter, whipped, with salt",
            "confidence": 0.45454545454545453
        },
        ...13 more items 
    ], 
    "sirloin steak": [
        {
            "fdcId": 174763,
            "name": "Beef, top sirloin, steak, separable lean only, trimmed to 1/8\" fat, choice, raw",
            "confidence": 0.3239130434782609
        },
        {
            "fdcId": 171804,
            "name": "Beef, top sirloin, steak, separable lean only, trimmed to 1/8\" fat, select, raw",
            "confidence": 0.3239130434782609
        },
        ...13 more items
    ]
}
```


#### Portions for Database Ingredient

Knowing that you have 3 cups of flour and that flour has 200 Calories per 100g is not enough to calculate the number of Calories in those 3 cups of flour. What we additionally need is portion data that tells us the weight in grams for a certain portion---i.e., 1 cup of flour is 50g. 

This is the second step in calculating the nutrition information for a recipe. We choose the database ingredients from the previous `ingredient-matches` step that most closely match our recipe ingredients (the original written ingredients). Given those chosen database ingredients, this endpoint returns all possible portion sizes that the database has for that ingredient. 

URL: `/api/recipes/portions`, with the FDC IDs of the chosen database ingredients included as request parameters.

Method: GET

Example use: `/api/recipes/portions?fdcIds=169230&fdcIds=173430&fdcIds=174763`

Request Body: N/A

Response Body: 
A JSON object that maps the FDC ID of each database ingredient to an array of JSON objects that were previously PortionModel objects. 

PortionModel objects contain the following attributes: `portionId`, which is the ID of the portion in the portions table; `fdcId`, which is the FDC ID of the database ingredient whose portions we are fetching; `unitAmount`, which is the quantity of the portion (often is just 1.0, but could be, for example, 1/8 if the ingredient is a pie); `unit`, which is the unit of the portion; `weight`, which is the weight of the portion in grams.  

Example Response:
```json
{
    "173430": [
        {
            "portionId": 92511,
            "fdcId": 173430,
            "unitAmount": 1.0,
            "unit": "pat (1\" sq, 1/3\" high)",
            "weightGrams": 5.0
        },
        {
            "portionId": 92512,
            "fdcId": 173430,
            "unitAmount": 1.0,
            "unit": "tbsp",
            "weightGrams": 14.2
        },
        {
            "portionId": 92514,
            "fdcId": 173430,
            "unitAmount": 1.0,
            "unit": "stick",
            "weightGrams": 113.0
        }
    ],
    "169230": [
        {
            "portionId": 84479,
            "fdcId": 169230,
            "unitAmount": 1.0,
            "unit": "tsp",
            "weightGrams": 2.8
        },
        {
            "portionId": 84480,
            "fdcId": 169230,
            "unitAmount": 1.0,
            "unit": "clove",
            "weightGrams": 3.0
        }
    ],
   "174763": null
}
```


#### Actual Calculation

This is the third and final step in calculating the nutrition of a recipe. Once we choose the relevant portions from the previous step, we pass them in for each ingredient along with a quantity, which is sufficient to calculate the nutrition for the whole recipe. Note that the user can always submit a weight as a portion rather than choosing a portion explicitly from the previous step, as any weight can be easily and immediately turned into grams. 

URL: `/api/recipes/calculate-nutrition`. The number of servings can optionally be included as a request parameter. 

Method: PUT

Example use: `/api/recipes/calculate-nutrition?servings=4`

Request Body: The request body contains a JSON array of JSON objects. These objects will be converted into IngredientSelectionModel objects once they reach the server. 

IngredientSelectionModel objects have the following attributes: `portionId` is the id for the chosen portion for the given ingredient, which can optionally be null if the user chooses the unit of the portion to be a weight; `fdcId` is the FDC ID of the ingredient this object corresponds to; `quantity` is the quantity of the portion---e.g., if the portion is 1/8 of a pie, the quantity could be 2 to get a total of 1/4 of the pie; `unit` is the unit of the portion, which either comes from the portion in the previous step or is directly a weight. 

Example Request Body: 
```json
[
    {
        "portionId": null,
        "fdcId": 174763,
        "quantity": 2.0,
        "unit": "lb"
    },
    {
        "portionId": 92512,
        "fdcId": 173430,
        "quantity": 4.0,
        "unit": "tbsp"
    },
    {
        "portionId": 84480,
        "fdcId": 169230,
        "quantity": 3.0,
        "unit": "clove"
    }
]
```

Response Body: If everything goes without error, the endpoint returns JSON that was previously a RecipeNutritionModel. If there is an error, an error string is returned.

In Java, this RecipeNutritionModel contains these attributes: `selections`, which is the List of IngredientSelectionModel objects that was submitted in the Request Body; `servings`, which gives the number of servings, or is null if no serving count was given; `totalNutrition` and `perServingNutrition`, which both have a NutritionValuesModel as a value, except that `perServingNutrition` is null if `servings` is null. 

A NutritionValuesModel object has attributes `calories` with units Calories, `protein` with unit grams, `fat` with units grams, `carbs` with units grams, `fiber` with units grams, `totalSugar` with units grams, `satFat` with units grams, `cholesterol` with units mg, and `sodium` with units mg. 

Response Example:
```json
{
    "selections": [
        {
        "portionId": null,
        "fdcId": 174763,
        "quantity": 2.0,
        "unit": "lb"
        },
        {
        "portionId": 92512,
        "fdcId": 173430,
        "quantity": 4.0,
        "unit": "tbsp"
        },
        {
        "portionId": 84480,
        "fdcId": 169230,
        "quantity": 3.0,
        "unit": "clove"
        }
    ],
    "servings": 4,
    "totalNutrition": {
        "calories": 1645.3590000000002,
        "protein": 199.81833799999998,
        "fat": 88.02719599999999,
        "carbs": 3.00948,
        "fiber": 0.189,
        "totalSugar": 0.12408,
        "satFat": 44.18946819999999,
        "cholesterol": 675.4997999999999,
        "sodium": 524.8706
    },
    "perServingNutrition": {
        "calories": 411.33975000000004,
        "protein": 49.954584499999996,
        "fat": 22.006798999999997,
        "carbs": 0.75237,
        "fiber": 0.04725,
        "totalSugar": 0.03102,
        "satFat": 11.047367049999998,
        "cholesterol": 168.87494999999998,
        "sodium": 131.21765
    }
}
``` 

