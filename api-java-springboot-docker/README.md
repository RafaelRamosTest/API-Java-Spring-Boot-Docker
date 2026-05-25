\# Projeto Spring Boot - Customer API



API REST desenvolvida em \*\*Spring Boot\*\* para cadastro e consulta de clientes, utilizando \*\*PostgreSQL\*\* como banco de dados.



\---



\## 🚀 Tecnologias

\- Java 17+

\- Spring Boot

\- Maven

\- PostgreSQL

\- JUnit + Mockito

\- Docker



\---



\## 📋 Pré-requisitos

\- \[Java 17+](https://adoptium.net/)

\- \[Maven](https://maven.apache.org/)

\- \[PostgreSQL](https://www.postgresql.org/)

\- \[Docker](https://www.docker.com/)

\- \[Docker Compose](https://docs.docker.com/compose/)



\---



\## ⚙️ Configuração do ambiente



\### Banco de Dados

Este projeto utiliza \*\*PostgreSQL\*\*. Crie o banco de dados local:



```sql

CREATE DATABASE customer\_db;



Configure o arquivo src/main/resources/application.properties com os parâmetros abaixo:

\# Porta do servidor

server.port=8081



\# Configuração do banco PostgreSQL

spring.datasource.url=jdbc:postgresql://localhost:5432/customer\_db

spring.datasource.username=seu\_usuario

spring.datasource.password=sua\_senha

spring.datasource.driver-class-name=org.postgresql.Driver



\# JPA / Hibernate

spring.jpa.hibernate.ddl-auto=update

spring.jpa.show-sql=true

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect





Como rodar localmente

Clone o repositório:

git clone https://github.com/seu-usuario/customer-api.git

cd customer-api



Exemplos de requisição

Cadastro de cliente:



curl -X POST http://localhost:8081/customer \\

 -H "Content-Type: application/json" \\

 -d '{

   "name": "Rafael",

   "email": "rafael@email.com",

   "cpf": "12345678900",

   "phone": "11999999999",

   "address": "Rua das Flores, 123",

   "city": "Carapicuíba",

   "password": "senhaSegura123",

   "zipcode": "06320-000",

   "terms": true

 }'



curl -X GET http://localhost:8081/customer/12345678900


Executando com Docker:

docker-compose up --build

http://localhost:8081/customer

curl -X POST http://localhost:8081/customer \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rafael",
    "email": "rafael@email.com",
    "cpf": "12345678900",
    "phone": "11999999999",
    "address": "Rua das Flores, 123",
    "city": "Carapicuíba",
    "password": "senhaSegura123",
    "zipcode": "06320-000",
    "terms": true
  }'


curl -X GET http://localhost:8081/customer/12345678900



Rodando os testes unitarios: mvn test

Os testes cobrem:

CustomerServiceTest → lógica de negócio.

CustomerControllerTest → endpoints REST com MockMvc.

