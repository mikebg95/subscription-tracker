# Subscription Tracker - Architecture & Diagrams
This repository contains the software architecture blueprints for the Subscription Tracker ecosystem using the **C4 Model** via **Structurizr**.

## How to View the Diagrams Locally
The architecture is written as code using the Structurizr DSL (`workspace.dsl`). You can run a local development server using Docker to view and interact with the live diagrams.

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

### Steps to Run
	1.	Open your terminal and navigate to this repository folder:
cd subscription-tracker-architecture
	2.	Run the following Docker command to spin up Structurizr Lite (this mounts your current folder containing the `workspace.dsl` file):
docker run -it --rm -p 9090:8080 -v "$(pwd)":/structurizr structurizr/lite
	3.	Open your web browser and navigate to:
**http://localhost:9090**

### Available Views
Once the browser UI loads, you can toggle between three conceptual layers:
	1.	**System Context:** High-level overview showing how the user interacts with the Subscription Tracker system.
	2.	**Container View:** Zoomed-in look at the Frontend (Angular), Backend API (Spring Boot), and Database (PostgreSQL).
	3.	**Component View:** Deep-dive into the Backend API's internal structure (Controllers, Services, DAOs).
    
## Repository Contents
* `workspace.dsl` - The source of truth architecture-as-code file.
* `.gitignore` - Configured to ignore auto-generated layout caching (`workspace.json`).