# Web Scraper Microservice Project

This repository contains a Web Scraper microservice to be used for scraping websites.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Technologies](#technologies)
- [Customizations](#customizations)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Docker Compose Sample Config](#docker-compose-sample-config)

## Introduction

This project maintains a Spring Boot-based microservice built for scraping websites, it supports to scrap a website loading its webpage as a static page or a dynamic page (mimicking a browser with JS support) and also to scrap an API service that returns a JSON response.
It takes advantage of multithreading to scrap a set of webpages simultaneously and employs IP rotation using different proxies/networks to prevent blocking from target websites.

## Features

- **RESTful API**: Exposes a RESTful API with services for scrap single or multiple target websites.
- **Web Scraper Runner and Default Web Scraper Runner**: Provides an interface to implement a task executor responsible for scraping content from the requested target websites. The Default Web Scraper Runner uses the WorkStealingPool implementation as the thread pool.
- **IP Rotation**: Switches between the Tor network and the host's own public IP address to rotate IP addresses.
- **Exception Handling**: Provides robust exception handling for better error management.
- **Logging**: Integrates log4j for effective debugging, monitoring, and log management.
- **Configuration Management**: Supports external configuration files to easily manage application settings.
- **Testing**: Includes comprehensive unit tests and integration tests using JUnit and Mockito for ensuring code quality.

## Technologies

The project utilizes the following technologies and tools:

- Spring Boot
- Java 20
- Fabric8 Docker Plugin (for Docker image build)
- Maven (for dependency management)
- JUnit and Mockito (for testing)

## Customizations

The microservice provides several configuration options that can be customized according to your requirements. These configurations can be modified in the application.properties file. Here's a brief explanation of each configuration:

- `connection.provider.default.max.active.connections`: Defines the maximum number of active connections supported for the default connection provider.
- `connection.provider.tor.hostname`: The hostname where the Tor proxy is running. If you are using the Docker image, it's recommended not to change it.
- `connection.provider.tor.port`: The port where the Tor proxy is running. If you are using the Docker image, it's recommended not to change it.
- `connection.provider.tor.restart.script.path`: The script executed when the Tor connection provider requests an IP renewal. If you are using the Docker image, it's recommended not to change it.
- `connection.provider.tor.wait.for.restart.script.timeout`: Defines the amount of time to wait for the Tor connection provider script to finish.
- `connection.provider.tor.wait.for.restart.script.unit`: Defines the units of time for the `connection.provider.tor.wait.for.restart.script.timeout` property.
- `connection.provider.tor.max.active.connections`: Defines the maximum number of active connections supported for the Tor connection provider.
- `connection.provider.tor.enabled`: Enables or disables the Tor connection provider.
- `runner.wait.for.scraper.response.timeout`: Defines the amount of time to wait for a scraped site response.
- `runner.wait.for.scraper.response.unit`: Defines the units of time for the `runner.wait.for.scraper.response.timeout` property.

You can also customize the default site to probe the external IP by setting the WEBSITE_TO_GET_EXTERNAL_IP environment variable with the desired URL. By default, it uses the URL https://api.ipify.org/.


## Getting Started

To get started with the project, follow these steps:

1. Install Docker and docker-compose or Docker Desktop (Windows).
2. Clone the repository: `git clone https://github.com/r-antonio16/web-scraper.git`.
3. Navigate to the project directory: `cd web-scraper`.
4. Build the project: `mvn clean install`.
5. Use the Docker image 'web-scraper:0.0.1-SNAPSHOT' to start a new container or refer to the [Docker Compose Sample Config](#docker-compose-sample-config) for starting a container using Docker Compose.

## Usage

Once the microservice is up and running, you can interact with it by making requests to the exposed RESTful endpoints using your preferred REST client or browser. 

Refer to the OpenAPI documentation available at http://localhost:8080/docs-ui.html to explore the available services in detail, including request payloads, response formats, and examples.

Please note that you may need to replace localhost:8080 with the appropriate host and port if you are running the microservice on a different server.

## Docker Compose Sample Config

```yaml
version: '3'
services:
  web-scraper:
    container_name: web-scraper
    image: zerodseis/web-scraper:0.0.1-SNAPSHOT
    ports:
      - '8000:8000'
      - '8080:8080'
    restart: always
```

Copy the above Docker Compose configuration into a `docker-compose.yml` file and run `docker-compose up -d` in the project directory to start the microservice container.