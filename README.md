🚀 BingBank Startup Instructions
Prerequisites

PostgreSQL database running on localhost:5432
Database: netbanking_db with all tables created
Java 17 installed
Node.js installed
Kafka installed at C:\kafka


Step 1: Start Kafka
Open Command Prompt and navigate to Kafka directory:
bashcd C:\kafka
Start Kafka server:
bashbin\windows\kafka-server-start.bat config\server.properties
Keep this terminal open! You should see logs indicating Kafka has started successfully.

Step 2: Start Backend Microservices
Open separate terminal windows for each service:
2.1 Auth Service (Port 8081)
bashcd auth-service
mvn spring-boot:run
2.2 Account Service (Port 8082)
bashcd account-service
mvn spring-boot:run
2.3 Transaction Service (Port 8083)
bashcd transaction-service
mvn spring-boot:run
2.4 Fixed Deposit Service (Port 8084)
bashcd fixed-deposit-service
mvn spring-boot:run
2.5 Fund Transfer Service (Port 8085)
bashcd fund-transfer-service
mvn spring-boot:run
2.6 API Gateway (Port 8080)
bashcd api-gateway
mvn spring-boot:run
Wait for all services to start! Look for Started [ServiceName]Application in X seconds in each terminal.

Step 3: Start Frontend
Open a new terminal:
bashcd netbanking-frontend
npm start
Frontend will start on http://localhost:3000 and should open automatically in your browser.

Step 4: Verify Everything is Running
Check Backend Services:

Auth Service: http://localhost:8081
Account Service: http://localhost:8082
Transaction Service: http://localhost:8083
Fixed Deposit Service: http://localhost:8084
Fund Transfer Service: http://localhost:8085
API Gateway: http://localhost:8080

Check Frontend:

React App: http://localhost:3000

Check Kafka:
You should see Kafka logs showing consumer group connections from fund-transfer-service.

📝 Quick Startup Order Summary

Kafka - Start first (keep running)
Auth Service - Port 8081
Account Service - Port 8082
Transaction Service - Port 8083
Fixed Deposit Service - Port 8084
Fund Transfer Service - Port 8085
API Gateway - Port 8080 (Start last among backend services)
Frontend - Port 3000


🛑 Shutdown Instructions
Shutdown Order (Reverse):

Stop Frontend (Ctrl+C)
Stop API Gateway (Ctrl+C)
Stop Fund Transfer Service (Ctrl+C)
Stop Fixed Deposit Service (Ctrl+C)
Stop Transaction Service (Ctrl+C)
Stop Account Service (Ctrl+C)
Stop Auth Service (Ctrl+C)
Stop Kafka (Ctrl+C)
