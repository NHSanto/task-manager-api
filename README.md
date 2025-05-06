# Task Management API

## Project Description

This is a Spring Boot-based REST API for managing tasks. It features JWT authentication, role-based access control, and integrates with a PostgreSQL database. The application allows admins and users to manage tasks, post comments, and authenticate via JWT tokens.

## Technologies Used

- **Java** (Spring Boot)
- **PostgreSQL** (Database)
- **Docker** (Containerization)
- **JWT** (Authentication)
- **Swagger/OpenAPI** (API Documentation)

## Prerequisites

- Docker and Docker Compose installed
- Java 21+ installed
- Maven or Gradle for building the project

## Setup Instructions

1. Clone the repository:
    ```bash
    git clone https://github.com/NHSanto/task-manager-api.git
    cd <project_directory>
    ```

2. Set up Docker:
    - Ensure Docker and Docker Compose are installed on your system.

3. Build the project:
    ```bash
    ./mvnw clean install
    ```

4. Start the db using Docker Compose:
    ```bash
    docker-compose up -d db
    ```

   This will set up PostgreSQL container as defined in the `docker-compose.yml`.
5. Start the application:
 ```bash
    mvn spring-boot:run
 ```

6. Visit the API at:
    - [http://localhost:8080](http://localhost:8080)

## Configuration

### Application Properties (`application.properties`)

- `spring.application.name=api`  
  Specifies the name of the Spring Boot application.

- **PostgreSQL Configuration**
    - `spring.datasource.url=jdbc:postgresql://localhost:5432/rest-api`
    - `spring.datasource.username=postgres`
    - `spring.datasource.password=12345`

- **JWT Configuration**
    - `jwt.access.path=classpath:jwt/access.txt`
    - `jwt.refresh.path=classpath:jwt/refresh.txt`

### Docker Compose Configuration (`docker-compose.yml`)

The `docker-compose.yml` file contains the services for PostgreSQL.

## Endpoints

### Auth Endpoints

- **POST** `/auth/login`: Log in and receive a JWT access token.
- **POST** `/auth/newAccessToken`: Get a new access token using a refresh token.
- **POST** `/auth/refreshToken`: Get a new access token and refresh token.
- **DELETE** `/auth/logout`: Log out by invalidating the refresh token.

### Task Endpoints

- **POST** `/task/new`: Create a new task.
- **PUT** `/task`: Update a task.
- **PATCH** `/task/{taskId}/status`: Update task status (Admin or Executor).
- **DELETE** `/task`: Delete a task.
- **GET** `/task/all`: Get all tasks.
- **GET** `/task/by-status`: Get all tasks by status.
- **GET** `/task/all/creator/{id}`: Get tasks by creator ID.
- **GET** `/task/all/executor/{id}`: Get tasks by executor ID.

