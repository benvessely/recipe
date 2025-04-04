#!/bin/bash
set -e

# Start PostgreSQL
service postgresql start

# Create database
sudo -u postgres psql -c "CREATE DATABASE recipe_app;"

# Set password
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"


# Configure application properties
mkdir -p src/main/resources
echo "spring.datasource.url=jdbc:postgresql://localhost:5432/recipe_app
spring.datasource.username=postgres
spring.datasource.password=postgres" > src/main/resources/application.properties

# Build project
chmod +x ./mvnw
# ./mvnw clean install

# Run tests
./mvnw test

echo "Installation test completed successfully!"
