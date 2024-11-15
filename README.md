# Polygon Tiling Service

A secure REST API service that generates tile footprints from GeoJSON areas of interest, with a web client for visualization.

## Features

- Secure API endpoints using Keycloak authentication
- GeoJSON input processing with support for Polygons and MultiPolygons
- Efficient tile generation using R-tree spatial indexing
- OpenAPI documentation
- Web client with Leaflet map visualization
- Comprehensive test coverage

## Prerequisites

- Java 21
- Maven 3.8+
- Docker and Docker Compose

## Getting Started

1. Start Keycloak:
```bash
docker-compose up -d
```

2. Import the realm configuration (wait for Keycloak to start first):
```bash
docker exec -i keycloak /opt/keycloak/bin/kc.sh import --file /opt/keycloak/data/import/realm-export.json
```

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

5. Access the application:
   - Web Client: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html
   - Keycloak Admin Console: http://localhost:8180/admin (admin/admin)

## Default Test User

- Username: test-user
- Password: test-password

## API Endpoints

POST /api/v1/tiles
- Accepts GeoJSON area of interest
- Returns tile footprints in GeoJSON format
- Requires valid JWT token

## Example GeoJSON Input

```json
{
  "type": "Feature",
  "geometry": {
    "type": "Polygon",
    "coordinates": [
      [[0,0], [0,1], [1,1], [1,0], [0,0]]
    ]
  }
}
```

## Testing

Run the test suite:
```bash
mvn test
```

## Security

This service uses Keycloak for authentication and authorization. The API endpoints are secured using OAuth2/JWT tokens.

## Configuration

Key configuration properties can be modified in `src/main/resources/application.properties`:
- `tiling.error-margin`: Controls the precision of tile generation
- `tiling.max-recursion-depth`: Limits the subdivision of tiles

## Architecture

- Spring Boot backend with OAuth2 resource server
- R-tree spatial indexing for efficient tile storage and retrieval
- Leaflet.js frontend for map visualization
- Keycloak for identity and access management

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
