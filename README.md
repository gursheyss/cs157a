# SJSU Event Manager

Spring Boot + React

## Structure

- `client/`: React frontend with Vite
- `server/`: Spring Boot backend
- `docker-compose.yml`: Docker setup
- `init-db.sql`: Database init script

## Requirements

- Node.js 18+
- pnpm 8+
- Java 21
- Maven
- Docker & Docker Compose

## Quick Start

Install deps:

```bash
# Get pnpm if needed
npm install -g pnpm

# Install everything
pnpm install
```

Start the database:

```bash
pnpm db:start
```

## Development

Frontend:

```bash
pnpm dev  # from root
# or
cd client && pnpm dev
```

Backend:

```bash
pnpm server  # from root
# or
cd server && ./mvnw spring-boot:run
```

Build:

```bash
# Frontend
pnpm build

# Backend
cd server && ./mvnw clean package
```

## Docker Commands

Database:

```bash
pnpm db:start  # start MySQL
pnpm db:stop   # stop MySQL
pnpm db:logs   # view logs
```

Full stack:

```bash
pnpm docker:build  # build images
pnpm docker:up     # start everything
pnpm docker:down   # stop all containers
pnpm docker:logs   # view all logs
```

Connect to MySQL:

```bash
docker exec -it sjsu_event_db mysql -usjsu_user -psjsu_password sjsu_events
```

> Note: The Vite dev server proxies `/api/*` requests to Spring Boot at `http://localhost:8080`
