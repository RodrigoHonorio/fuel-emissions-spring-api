# 🌍 S.P.I.R.E. Fuel Emissions Calculator

An enterprise-grade Spring Boot REST API designed to calculate Volatile Organic Compounds (VOC) emissions for petrol station operations based on technical inventory methodologies.

## 🚀 Technologies & Stack

* **Java 17**
* **Spring Boot 3.3.2** (Web, Data JPA)
* **Maven** (Build and dependency management)
* **H2 Database** (In-memory database for rapid persistence and testing)
* **JUnit 5 & Mockito / MockMvc** (Automated web layer testing)
* **HTML5 / Frontend Integration**

## 🏛️ Architecture & Design Patterns

The project follows a clean **MVC (Model-View-Controller)** architectural pattern:
* **Controller Layer (`/api/emissions/calculate`)**: Handles REST requests and returns structured ResponseEntity responses.
* **Service Layer**: Encapsulates core business logic and calculation algorithms.
* **Repository Layer**: Manages data persistence using Spring Data JPA.
* **DTO Records**: Immutable data transfer objects ensuring robust payload handling.
* **Global Exception Handling (`@RestControllerAdvice`)**: Centralised error management with custom exceptions (`InvalidEmissionDataException`).

## 🧪 Testing

Includes automated web layer testing using `@WebMvcTest` and `MockMvc` to validate controller endpoints and HTTP status contracts safely without booting the full server stack.

## ⚙️ Getting Started

1. Clone the repository:
   ```bash
   git clone [https://github.com/RodrigoHonorio/fuel-emissions-spring-api.git](https://github.com/RodrigoHonorio/fuel-emissions-spring-api.git)
