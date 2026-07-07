# Activity Service API 🚀

Uma API robusta desenvolvida em Java com Spring Boot utilizando arquitetura orientada a eventos (EDA). O ecossistema gerencia o registro e monitoramento de atividades integrado ao Apache Kafka e persistência poliglota utilizando MongoDB local corporativo/físico fora do ecossistema de containers do Docker.

---

## 🛠️ Tecnologias Utilizadas

* **Java 17** & **Spring Boot 3.x**
* **Spring Security 6** (Autenticação Stateless baseada em JWT do Auth0)
* **Apache Kafka** (Zookeeper + Kafka Broker para comunicação assíncrona)
* **MongoDB** (Banco de dados NoSQL executado nativamente na máquina hospedeira)
* **Jackson Datatype JSR310** (Serialização avançada de tipos temporais do Java 8)
* **Docker & Docker Compose** (Orquestração simplificada de infraestrutura)

---

## 📐 Arquitetura e Fluxo de Dados

A arquitetura do projeto divide-se em dois fluxos principais monitorados, ambos blindados pela camada de filtros do Spring Security e centralizados pelo uso de **Enums de Configuração** para isolar as Strings de infraestrutura das regras de negócio.

### 1. Fluxo de Criação (POST)
1. O cliente faz uma requisição HTTP POST autenticada para `/activities/create`.
2. O `ActivityController` valida o Token JWT e injeta os metadados do usuário logado (`@AuthenticationPrincipal`).
3. A `ActivityService` intercepta o dado, processa e dispara o payload estruturado para o tópico do Kafka definido no Enum (`KafkaConfigEnum.ATIVIDADES`).
4. O `ActivityConsumer` escuta a fila via expressão dinâmica e faz o descarregamento assíncrono do registro na coleção `activities` do MongoDB local (`host.docker.internal`).

### 2. Fluxo de Consulta e Auditoria (GET)
1. O cliente faz uma chamada HTTP GET para `/activities`.
2. A API consome os dados atualizados de um serviço externo via `RestTemplate`.
3. Antes de responder ao cliente, a Service gera um instantâneo de telemetria contendo o carimbo de data/hora (`LocalDateTime`) e a **lista completa com todos os 30 registros retornados agrupados em lote**.
4. Esse pacote de log é serializado e postado no tópico de auditoria (`KafkaConfigEnum.LOGS`).
5. O consumidor reidrata o payload tratando o fuso horário e salva o histórico unificado na coleção `activity_logs`.

---

## 📂 Estrutura de Pacotes Relevante

A distribuição de responsabilidades para o uso de Enums e infraestrutura segue o padrão abaixo:

```text
com.exemple.activity/
│
├── config/                  # Filtros de Segurança (JWT)
│   └── SecurityConfig.java
│
├── controller/              # Endpoints expostos expurgados de regras redundantes
│   └── ActivityController.java
│
├── enums/                   # Tipagem estática (Tópicos do Kafka e IDs de Grupo)
│   └── KafkaConfigEnum.java
│
├── model/                   # Modelos de dados persistidos no Mongo local
│   ├── Activity.java
│   └── ActivityLog.java
│
├── service/                 # Camada de negócios, Produtores e Consumidores
│   ├── ActivityService.java
│   ├── ActivityProducer.java
│   └── ActivityConsumer.java
└── repository/              # Spring Data MongoDB Repositories
🚀 Como Executar o ProjetoPré-requisitosTer o Docker e Docker Compose instalados na máquina.Ter o MongoDB instalado localmente na máquina hospedeira rodando na porta padrão 27017.1. Configurando as Variáveis de Ambiente (application.yml)Certifique-se de que a URI do MongoDB aponta para o gateway do Docker (host.docker.internal no Windows/Mac ou o IP da ponte no Linux) para furar o isolamento do container e salvar na sua máquina física:YAMLspring:
  data:
    mongodb:
      uri: mongodb://host.docker.internal:27017/activity_db
2. Subindo a Infraestrutura com Build LimpoComo o projeto utiliza bibliotecas específicas de tempo do Java 8 para o Jackson no Kafka, limpe os caches de imagens anteriores do Docker para compilar o Maven de forma íntegra:Bash# Derruba os containers limpando volumes antigos
docker compose down -v

# Compila a aplicação ignorando caches de builds anteriores
docker compose build --no-cache activity-service

# Sobe os serviços em segundo plano (API + Kafka + Zookeeper)
docker compose up -d
📡 Endpoints DisponíveisTipoEndpointDescriçãoProteçãoPOST/activities/createCria e publica uma nova atividade individualRequer JWT válidoGET/activitiesConsulta todas as atividades da API externa e gera log de auditoria completoRequer JWT válido🔍 Monitoramento dos Dados (MongoDB Compass)Para visualizar as suas coleções povoadas em tempo real direto da sua máquina local:Abra o MongoDB Compass.Conecte-se à string padrão local do seu sistema operacional:Plaintextmongodb://localhost:27017/
Navegue até o banco de dados activity_db.Monitore as coleções separadas:activities: Armazena registros individuais gerados pelo POST.activity_logs: Armazena documentos de auditoria contendo sub-arrays estruturados com o histórico unificado de requisições disparadas pelo GET.