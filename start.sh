#!/bin/bash

echo "Starting Keycloak..."
docker-compose down
docker-compose up -d

echo "Waiting for Keycloak to start..."
until curl -s --head http://localhost:8180; do
    echo "Waiting for Keycloak..."
    sleep 5
done

echo "Building and starting the application..."
mvn clean install
mvn spring-boot:run
