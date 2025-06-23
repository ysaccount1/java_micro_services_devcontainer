# Development Container for Java Microservices

This directory contains configuration for a VS Code development container that provides a consistent development environment for the Java microservices project.

## Features

- Java 17 development environment
- Maven for dependency management
- Node.js for frontend development
- Docker CLI for container management
- Redis for caching
- Spring Boot services with hot reload
- React frontend with hot reload

## Getting Started

1. Install [VS Code](https://code.visualstudio.com/) and the [Remote - Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) extension.
2. Open this project in VS Code.
3. When prompted, click "Reopen in Container" or use the command palette (F1) and select "Remote-Containers: Reopen in Container".
4. VS Code will build the development container and connect to it. This may take a few minutes the first time.

## Services

- **Auth Service**: http://localhost:8082
- **Shopping Service**: http://localhost:8081
- **Frontend**: http://localhost:3000
- **Redis**: localhost:6379

## Debugging

- Auth Service: Debug port 5005
- Shopping Service: Debug port 5006

## Notes

- The Maven repository is stored in a Docker volume for better performance and to avoid corruption.
- Source code changes are synchronized with the containers in real-time.
- The frontend uses environment variables for API URLs that work both in development and production.