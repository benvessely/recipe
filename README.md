# Nutrition Calculator
Created by Ben Vessely, who can be contacted at benvessely at gmail dot com. 

## Description 
This is a Java Spring Boot project with a RESTful API that can be used to calculate the nutrition information of a recipe. Since this first version of the project lacks a frontend component, the project is made up of a series of API calls that take on different responsibilities in the workflow. The data being used for nutrition information is from GET THIS. 

My base idea for this project was just to perform CRUD operations on recipes, with the recipes being stored in a PostgreSQL database, and this functionality is still intact. Once a recipe is in the database, there is an `ingredient-matches` endpoint that can be used to fetch the top 10 most relevant matches from the ingredient database, with relevance determined by a multi-part matching algorithm. The user then picks the database ingredient that most closely matches what the user was searching for for each original ingredient (Note: we differentiate between "original ingredient", which is what the user gives in the recipe, and "database ingredient", which is an element from the food database; in this document, the difference between original and database ingredients will sometimes be implied via context). 

Once the selection of database ingredients has been made, these choices are passed into the `portions` endpoint. This endpoint takes care of finding all possible portion data for each chosen database ingredient. This portion data is needed because the nutritional information is stored in units per gram in the database, so for user inputs like "1 cup flour" and "4 carrots", we need a way to convert those units to grams. 

These portion options are then returned to the user, who chooses the most relevant ones for their recipe (for example, the portions for the previous examples might be 1 cup of flour is 50g, and a medium-sized carrot is 25g). The user then passes the chosen portions the quantity of those portions into the `calculate-nutrition` endpoint. This endpoint then calculates the final recipe nutrition and returns it to the user.

## Installation

