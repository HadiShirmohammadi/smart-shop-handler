# Smart Shop Handler

A full-stack online shopping application with an AI assistant and a Telegram bot. The project combines a **Java 21 / Spring Boot REST API**, a **React + Vite storefront**, and a **Python Telegram bot** that lets users browse product categories, search the catalog, and send text prompts to the AI service.

> **Project status:** Work in progress. The repository includes the storefront, REST API, and an initial Telegram bot implementation. Review the limitations and security notes before deploying it publicly.

## Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology stack](#technology-stack)
- [Repository structure](#repository-structure)
- [Prerequisites](#prerequisites)
- [Setup and run](#setup-and-run)
  - [1. PostgreSQL](#1-postgresql)
  - [2. Configure and start the Java backend](#2-configure-and-start-the-java-backend)
  - [3. Configure and start the frontend](#3-configure-and-start-the-frontend)
  - [4. Create an administrator](#4-create-an-administrator)
  - [5. Configure and start the Telegram bot](#5-configure-and-start-the-telegram-bot)
- [Environment variables and required files](#environment-variables-and-required-files)
- [REST API overview](#rest-api-overview)
- [Authentication and access control](#authentication-and-access-control)
- [Tests](#tests)
- [Deployment notes](#deployment-notes)
- [Known limitations](#known-limitations)

## Features

### Storefront
- Responsive storefront with product discovery, product details, search, tag filtering, and price/name sorting.
- English and Persian (RTL) interface.
- Light and dark themes.
- Account registration, login, profile view, and account deletion.
- Shopping cart with quantity updates, item removal, clear-cart action, and discounted subtotal calculation.
- Administrator screen to add and delete products, upload product images, and view registered users.
- Category-aware catalog and product organization.
- AI assistant chat screen.

### Java backend
- REST API built with Spring Boot and Spring Data JPA.
- PostgreSQL persistence for users, products, tags, categories, carts, cart items, and AI message records.
- BCrypt password hashing and stateless JWT authentication.
- Role-based access for administrative operations.
- Product search by product name or tag name, case-insensitively.
- Hierarchical categories with a category tree and product lookup across a category and its descendants.
- JPEG/PNG product image upload with server-generated filenames, image decoding/re-encoding, a 5 MB upload limit, and a 20-megapixel image limit.
- Groq chat-completions integration using the OpenAI-compatible API.

### Telegram bot
- `/start` welcome message and main menu.
- Browse the category tree, child categories, and products in a category.
- Inline product search.
- Send text prompts to the AI assistant.
- Photo messages can be submitted to the backend endpoint; **the current AI service only sends text to the model**, so image understanding is not implemented.

## Architecture

```text
React + Vite storefront
        |
        | HTTP / JSON, JWT for protected endpoints
        v
Spring Boot REST API --------------------> PostgreSQL
        |
        +--------------------------------> Groq Chat Completions API
        ^
        | HTTP / multipart form data
Python Telegram bot (aiogram)
```

The frontend uses Vite's `/backend` development proxy to forward API requests to the Java backend. The Telegram bot calls the Java API directly and does not use browser CORS.

## Technology stack

| Area | Technologies |
| --- | --- |
| Language / runtime | Java 21, Python 3.10+ |
| Backend | Spring Boot 4.1.1, Spring Web MVC, Spring Security, Spring Data JPA |
| Database | PostgreSQL |
| Authentication | JWT (JJWT 0.11.5), BCrypt |
| AI provider | Groq-compatible chat completions API; model `openai/gpt-oss-20b` |
| Frontend | React 19, Vite 6, lucide-react |
| Telegram | aiogram 3, aiohttp, aiofiles |
| Build / tests | Maven Wrapper, npm, Node.js test runner, JUnit |

## Repository structure

```text
smart-shop-handler/
├── mainServer/
│   ├── javaServer/
│   │   ├── src/main/java/assistant/main/javaServer/
│   │   │   ├── Config/       # JWT filter, Spring Security, CORS
│   │   │   ├── Controllers/  # REST controllers
│   │   │   ├── Dto/          # Request/response DTOs
│   │   │   ├── Entities/     # JPA entities
│   │   │   ├── Repository/   # Spring Data repositories
│   │   │   └── Services/     # Business logic and AI integration
│   │   ├── src/main/resources/application.properties
│   │   └── pom.xml
│   └── frontend/
│       ├── src/
│       │   ├── lib/           # API client, formatting, translations, tests
│       │   ├── App.jsx
│       │   ├── styles.css
│       │   └── theme.css
│       ├── .env.example
│       └── package.json
└── telBot/
    ├── main.py                # Telegram bot handlers
    └── ragData.py
```

Uploaded product images are stored at the configured `SHOP_UPLOAD_DIRECTORY` (default `uploads/products`) relative to the backend process's working directory unless an absolute path is configured.

## Prerequisites

Install the following before starting the project:

- JDK 21
- PostgreSQL
- Node.js compatible with Vite 6 (Node.js 20.19+ recommended)
- Python 3.10+ (for the Telegram bot)
- A Groq API key (for AI endpoints)
- A Telegram bot token from [@BotFather](https://t.me/BotFather) (for the bot)

## Setup and run

Commands below use PowerShell on Windows where noted. Run the backend, frontend, and bot in separate terminals.

### 1. PostgreSQL

Create a database named `shop`:

```sql
CREATE DATABASE shop;
```

The backend is configured for PostgreSQL on `localhost:5432`, with database username `postgres`. Hibernate uses `spring.jpa.hibernate.ddl-auto=update` to update the schema automatically during development.

### 2. Configure and start the Java backend

Set these environment variables before launching the backend.

**PowerShell**
```powershell
$env:DB_PASSWORD = "your-postgres-password"
$env:JWT_SECRET = "replace-with-a-random-secret-at-least-32-bytes-long"
$env:GROQ_API_URL = "https://api.groq.com/openai/v1"
$env:GROQ_API_KEY = "your-groq-api-key"
```

**macOS / Linux**
```bash
export DB_PASSWORD="your-postgres-password"
export JWT_SECRET="replace-with-a-random-secret-at-least-32-bytes-long"
export GROQ_API_URL="https://api.groq.com/openai/v1"
export GROQ_API_KEY="your-groq-api-key"
```

Start the backend:

```powershell
cd mainServer/javaServer
.\mvnw.cmd spring-boot:run
```

On macOS/Linux, use `./mvnw spring-boot:run`.

The API should be available at `http://localhost:8080`.

**Required AI prompt file:** `AiService` loads `text/sysPm.txt` relative to the backend process's working directory. This file is not included in the supplied repository archive. Create `mainServer/javaServer/text/sysPm.txt` with the system instructions for your assistant, and launch the backend from `mainServer/javaServer` so the relative path resolves. If you do not configure this file, the Spring application will fail during startup.

### 3. Configure and start the frontend

```powershell
cd mainServer/frontend
Copy-Item .env.example .env
npm install
npm run dev
```

Open `http://127.0.0.1:5173`.

The Vite development proxy forwards `/backend/...` requests to `http://localhost:8080` by default. To change the backend target, set `BACKEND_URL` in `mainServer/frontend/.env`. For a production API origin, `VITE_API_BASE_URL` can be configured before building; otherwise configure a same-origin reverse proxy for `/backend`.

Available scripts:

```bash
npm run dev
npm run build
npm run preview
npm test
```

### 4. Create an administrator

Public registration always creates a regular user. After registering, promote the account directly in PostgreSQL:

```sql
UPDATE users
SET is_admin = TRUE
WHERE username = 'your-username';
```

Sign out and sign in again so the new JWT contains the updated role. Only grant administrator privileges to trusted accounts.

### 5. Configure and start the Telegram bot

1. Create a bot with [@BotFather](https://t.me/BotFather) and copy its token.
2. Run `/setinline` in BotFather and enable inline mode if you want to use inline product search.
3. Configure the bot environment variables.

**PowerShell**
```powershell
$env:BOT_TOKEN = "your-telegram-bot-token"
$env:JAVA_SERVER = "http://localhost:8080/"
```

**macOS / Linux**
```bash
export BOT_TOKEN="your-telegram-bot-token"
export JAVA_SERVER="http://localhost:8080/"
```

`JAVA_SERVER` must end with `/`, because the bot builds endpoint URLs by concatenating paths.

Install dependencies and run:

```powershell
cd telBot
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install aiogram aiohttp aiofiles
python main.py
```

On macOS/Linux, activate with `source .venv/bin/activate`.

**Required welcome file:** `main.py` reads `welcomeMessage.txt` from the current working directory. This file is not included in the supplied repository archive. Create `telBot/welcomeMessage.txt` (UTF-8) and start the bot from the `telBot` directory.

The bot uses long polling; a public webhook URL is not required for local use. The Java backend must be running before the bot can browse categories or call the AI endpoint.

## Environment variables and required files

### Backend

| Variable / setting | Purpose | Required |
| --- | --- | --- |
| `DB_PASSWORD` | Password for the PostgreSQL `postgres` user | Yes |
| `JWT_SECRET` | HMAC signing secret for JWTs; use a strong secret of at least 32 bytes | Yes |
| `GROQ_API_URL` | OpenAI-compatible API base URL, normally `https://api.groq.com/openai/v1` | Yes for startup |
| `GROQ_API_KEY` | API key for the configured AI provider | Yes for AI use / currently injected at startup |
| `SHOP_UPLOAD_DIRECTORY` | Product image storage directory | No; defaults to `uploads/products` |
| `text/sysPm.txt` | System prompt read by `AiService` at startup | Yes for startup |

The database URL, username, Hibernate DDL mode, and multipart limits are currently configured in `application.properties`. Do not commit secrets or real API keys.

### Frontend

| Variable | Purpose | Default |
| --- | --- | --- |
| `BACKEND_URL` | Vite proxy target for the Java API | `http://localhost:8080` |
| `VITE_API_BASE_URL` | Optional API base URL used by the frontend | `/backend` |

The frontend stores the login session in browser `sessionStorage`; it is not designed as a persistent “remember me” session.

### Telegram bot

| Variable / file | Purpose |
| --- | --- |
| `BOT_TOKEN` | Token issued by BotFather |
| `JAVA_SERVER` | Java API base URL, including trailing slash |
| `welcomeMessage.txt` | UTF-8 welcome text loaded at startup |

Never publish your bot token. If it is exposed, revoke it through BotFather.

## REST API overview

Protected endpoints expect:

```http
Authorization: Bearer <jwt-token>
```

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Public | Register an account and return a JWT |
| `POST` | `/auth/login` | Public | Authenticate and return a JWT |
| `GET` | `/auth/profile` | Authenticated | Get the current user's profile |
| `DELETE` | `/auth/delete` | Authenticated | Delete the current account |
| `GET` | `/auth/users` | Admin | List registered users |
| `GET` | `/product/products` | Public | List products |
| `GET` | `/product/{id}` | Public | Get a product by ID |
| `GET` | `/product/search/{key}` | Public | Search by product name or tag |
| `POST` | `/product/add` | Admin | Create a product |
| `DELETE` | `/product/delete/{id}` | Admin | Delete a product |
| `POST` | `/product/images` | Admin | Upload a JPEG/PNG image (`multipart/form-data`, field `file`) |
| `GET` | `/product/images/{filename}` | Public | Retrieve a stored product image |
| `GET` | `/category/tree` | Public | Get the category tree |
| `GET` | `/category/{id}/children` | Public | List a category's immediate children |
| `GET` | `/category/{id}/products` | Public | List products in a category and its descendants |
| `POST` | `/category/add` | Admin | Create a category (optional `parentId`) |
| `DELETE` | `/category/{id}/delete` | Admin | Delete a category |
| `POST` | `/cart/items` | Authenticated | Add or adjust a cart item (`productId`, `quantity`) |
| `GET` | `/cart/get` | Authenticated | Get the current user's cart |
| `DELETE` | `/cart/delete` | Authenticated | Clear the current user's cart |
| `POST` | `/api/ai/message` | Public currently | Send a text prompt to the AI service |
| `POST` | `/api/bot/multifile` | Public currently | Receive Telegram-style multipart message data and return an AI reply |

For `/cart/items`, the request body is represented by `CartItemRequest`; a positive quantity adds/increases an item, while negative quantities are used to reduce it. Check the implementation before depending on edge cases in a production workflow.

The AI endpoint `/api/ai/message` accepts a raw request body containing the prompt text. The bot endpoint `/api/bot/multifile` expects multipart fields `user_id`, `chat_id`, `message_id`, `msg_type`, and optional `username`, `content`, and `file`.

## Authentication and access control

- Passwords are hashed with BCrypt.
- JWT authentication is stateless.
- Registration forces `isAdmin` to `false`; administrator promotion is a database operation.
- Product creation/deletion and image upload are intended for administrators. Category creation/deletion is restricted by Spring Security to the `ADMIN` role.
- Product browsing, category browsing, image retrieval, registration, and login are public.
- **Important:** `/api/ai/**` and `/api/bot/**` are public in the current security configuration. Add authentication where appropriate, request validation, rate limiting, abuse controls, and usage limits before exposing the service to the internet.
- CORS is currently configured for local frontend origins on ports `5500`, `5173`, and `4173` (with `localhost`/`127.0.0.1` variants as defined in `SecurityConfig`). Update it deliberately for your deployment domain.

## Tests

Backend tests:

```powershell
cd mainServer/javaServer
.\mvnw.cmd test
```

Frontend unit tests:

```powershell
cd mainServer/frontend
npm test
```

The current test suite includes backend application-context, CORS, and product-image service tests, plus frontend formatting and internationalization tests. The Telegram bot has no automated tests in this repository.

## Deployment notes

- Build the frontend with `npm run build`; deploy the generated `dist/` directory behind HTTPS.
- Route `/backend` to the Java API and remove the `/backend` prefix, or configure `VITE_API_BASE_URL` at build time and allow the production origin in backend CORS.
- Set `SHOP_UPLOAD_DIRECTORY` to a stable, writable absolute path and back up uploaded images.
- Keep database credentials, JWT secrets, Groq keys, and Telegram tokens out of source control and frontend bundles.
- Use HTTPS, restrictive request limits, rate limiting, logging, and a production-grade secret-management strategy.
- Replace `spring.jpa.hibernate.ddl-auto=update` with a managed schema migration strategy for production.
- The Telegram bot should run as a long-lived process under a process manager or container.
- Telegram inline-result thumbnails are currently unset. If thumbnails are added later, their image URLs must be reachable by Telegram over the public internet.

## Known limitations

- There is no checkout, payment processing, order management, product editing, or profile editing.
- The AI assistant currently sends only text plus a system prompt to Groq. Uploaded photos are not passed to a vision model, and AI requests do not include conversation history or live catalog/cart context.
- The frontend's chat history exists only while the assistant screen remains open.
- The Telegram bot has no integration with shop-account authentication, cart management, or orders.
- The bot's `ragData.py` is empty; retrieval-augmented generation is not implemented in the current bot code.
- `welcomeMessage.txt` and `text/sysPm.txt` are referenced by the code but are absent from the supplied archive; create these files before running the bot/backend as described above.
- Deleting products, categories, or users with related records may fail because of database relationships; deletion behavior should be tested with realistic data.
- Product deletion does not currently remove the corresponding image file.
- The API does not define a currency. The Telegram inline search currently labels prices as `تومان`, while the frontend labels values as generic backend units.
- The application has not been verified end-to-end against a fully configured production environment.

---

## License

No license file was present in the supplied archive. Add a `LICENSE` file if you want to explicitly define how others may use, modify, and distribute the project.
