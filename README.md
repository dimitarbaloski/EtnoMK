# EtnoMK

## Prerequisites

Before starting the application, install:

* [Docker](https://www.docker.com/)
* Docker Compose

## Environment configuration

Create a `.env` file in the root directory of the project based on the provided `.env.example` file.

On Windows:

```bash
copy .env.example .env
```

On Linux or macOS:

```bash
cp .env.example .env
```

Open the newly created `.env` file and replace the example values with the required configuration values.

> The `.env` file may contain sensitive information and should not be committed to Git.

## Running the application

From the root directory of the project, build and start all services with:

```bash
docker-compose up --build
```

Alternatively, with newer Docker Compose versions:

```bash
docker compose up --build
```

To run the application in the background:

```bash
docker-compose up --build -d
```

Docker Compose will start the following services:

* React frontend
* Spring Boot backend
* PostgreSQL database
* Image-similarity service

After all containers have started, open the application in your browser:

```text
http://localhost
```

## Stopping the application

To stop the running containers, press `Ctrl + C`, or run:

```bash
docker-compose down
```

To stop the containers and remove the associated database volume:

```bash
docker-compose down -v
```

> Warning: The `-v` option deletes the persisted database data.

