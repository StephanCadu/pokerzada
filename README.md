# Pokerzada

Monorepo inicial para gerenciar noites de poker.

## Estrutura

- `backend/`: Spring Boot + Java 17 + PostgreSQL
- `frontend/`: Next.js + TypeScript
- `Dockerfile.backend`: imagem para build e execução do backend
- `Dockerfile.frontend`: imagem para build e execução do frontend
- `docker-compose.yml`: orquestração com PostgreSQL, backend e frontend

## Como rodar localmente

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Docker

```bash
docker compose up --build
```

## Endpoints iniciais

- Backend: `http://localhost:8080/api/health`
- Frontend: `http://localhost:3000`
