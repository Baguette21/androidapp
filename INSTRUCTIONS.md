# ECTrivia Platform Setup Instructions

This document provides instructions on how to set up the local development environment for the ECTrivia Platform, specifically the Database and Kafka infrastructure.

## Prerequisites

*   [Docker Desktop](https://www.docker.com/products/docker-desktop) installed and running.
*   Java Development Kit (JDK) 17 installed.
*   Maven installed (or use the provided `mvnw` wrapper if available).

## Infrastructure Setup (Database & Kafka)

We use Docker Compose to spin up MySQL, Zookeeper, Kafka, and Kafka UI.

1.  Navigate to the `docker` directory:
    ```bash
    cd docker
    ```

2.  Start the services:
    ```bash
    docker-compose up -d
    ```

    This will start the following services:
    *   **MySQL Database**:
        *   Port: `3306`
        *   Database: `ectrivia_db`
        *   Username: `ectrivia`
        *   Password: `ectrivia123`
        *   Root Password: `root`
        *   It will automatically initialize with `init.sql`.
    *   **Apache Kafka**:
        *   Port: `9092` (exposed to localhost)
    *   **Zookeeper**:
        *   Port: `2181`
    *   **Kafka UI**:
        *   Port: `8080`
        *   URL: [http://localhost:8080](http://localhost:8080) (Use this to inspect topics and messages)

3.  Verify the services are running:
    ```bash
    docker-compose ps
    ```
    You should see all services with a status of `Up`.

4.  To stop the services:
    ```bash
    docker-compose down
    ```
    (Add `-v` to remove volumes/data if needed: `docker-compose down -v`)

## Building the Application

Navigate back to the project root and build the project using Maven:

```bash
mvn clean install
```
