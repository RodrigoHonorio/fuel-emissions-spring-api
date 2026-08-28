# 🌍 S.P.I.R.E. - Spatial Prediction & Emissions API

An enterprise-grade Spring Boot REST API designed to calculate Volatile Organic Compounds (VOC) emissions for petrol station operations and monitor real-time environmental data. Built with a vision to integrate spatial prediction, public health metrics, and environmental engineering.

## ✨ Key Features

* **Fuel Emission Calculator:** Calculates environmental impact based on robust technical methodologies (inspired by CETESB standards).
* **Live London Air Quality Integration:** Consumes real-time public data from the Transport for London (TfL) API to monitor PM10, PM2.5, and NO2 pollution bands.
* **Unified Interactive Dashboard:** A responsive frontend (HTML5/Bootstrap 5) that seamlessly combines the emissions calculator and real-time London pollution metrics.
* **OpenAPI Documentation:** Fully documented and testable endpoints via Swagger UI.

## 🚀 Technologies & Stack

* **Java 21 (LTS)**
* **Spring Boot 3.3.2** (Web, Data JPA, RestClient)
* **Maven** (Build and dependency management)
* **H2 Database** (In-memory database for rapid persistence and testing)
* **Swagger / OpenAPI 3** (API Documentation)
* **JUnit 5 & Mockito / MockMvc** (Automated testing)
* **Bootstrap 5** (Frontend Dashboard)

## 🏛️ Architecture & Design Patterns

The project follows a clean **MVC** architecture with Domain-Driven concepts:
* **Controller Layer**: Handles REST requests (`/api/emissions` and `/api/v1/london/air-quality`) returning structured `ResponseEntity` payloads.
* **Service Layer**: Encapsulates core business logic and calculation algorithms.
* **Integration Layer**: Utilizes Spring's modern `RestClient` to securely consume external government APIs (TfL).
* **Repository Layer**: Manages data persistence using Spring Data JPA / Hibernate.
* **DTO Records**: Immutable data transfer objects utilizing `@JsonIgnoreProperties` for robust external payload handling.
* **Global Exception Handling (`@RestControllerAdvice`)**: Centralised error management with custom exceptions.

## 🧪 Testing

Includes comprehensive automated testing ensuring pipeline reliability:
* **Controller Testing:** `@WebMvcTest` and `MockMvc` to validate HTTP status contracts safely without booting the full server.
* **Integration Testing:** `@RestClientTest` to mock external API servers and validate robust JSON deserialization and DTO mapping.

## ⚙️ Getting Started

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/RodrigoHonorio/fuel-emissions-spring-api.git](https://github.com/RodrigoHonorio/fuel-emissions-spring-api.git)