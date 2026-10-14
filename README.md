# Smart Assistant

A full-stack online shop with an AI chat assistant. The project consists of a **Spring Boot REST API** (JWT authentication, products, tags, search, image upload, shopping cart, AI proxy) and a **React + Vite** storefront ("Gather") that talks to it.

> **Status:** work in progress. The shop backend and web frontend are usable; the Telegram bot is **not completed yet** (see [Project status](#project-status)).

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [API overview](#api-overview)
- [Running the tests](#running-the-tests)
- [Production notes](#production-notes)
- [Project status](#project-status)
- [Known limitations](#known-limitations)

---

## Features

**Accounts**
- Registration, login, profile, and self-account deletion
- Passwords hashed with BCrypt; stateless JWT sessions (12-hour tokens)
- Role-based access: regular users and administrators (registration always creates a non-admin account)

**Catalog**
- Product listing with name, short/long description, integer price, percentage discount, image, and tags
- Server-side search by product name or tag name (case-insensitive)
- Admin-only product creation and deletion
- Admin-only image upload (JPEG/PNG, up to 5 MB). Images are decoded and re-encoded, limited to 20 megapixels, and saved under generated filenames

**Shopping cart**
- Add, increase, decrease, and remove items; clear the whole cart
- Cart responses use a DTO (no user data, no recursive entity references)
- Discounted-price subtotal in the frontend

**AI assistant**
- Chat endpoint that forwards a text message and a system prompt to an OpenAI-compatible chat API (Groq, model `openai/gpt-oss-20b`) and stores the exchange in the database
- Chat screen in the web frontend

**Frontend**
- Discover page with search, tag filtering, sorting, and product details
- Account, bag, administration, and AI assistant screens
- Light/dark theme toggle

---

## Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.1, Spring Web MVC, Spring Security, Spring Data JPA |
| Auth | JWT ([jjwt](https://github.com/jwtk/jjwt) 0.11.5), BCrypt |
| Database | PostgreSQL |
| AI | Groq (OpenAI-compatible chat completions API) |
| Frontend | React 19, Vite 6, lucide-react |
| Build tools | Maven Wrapper, npm |

---

## Project structure

```
smart_assistant/
├── mainServer/
│   ├── javaServer/        # Spring Boot REST API
│   │   └── src/main/java/assistant/main/javaServer/
│   │       ├── Config/        # Security, CORS, JWT filter
│   │       ├── Controllers/   # Auth, Product, ProductImage, Cart, Bot
│   │       ├── Services/
│   │       ├── Repository/
│   │       ├── Entities/      # User, Product, Tag, Cart, CartItem, Message
│   │       └── Dto/
│   ├── frontend/          # React + Vite storefront
│   └── uploads/products/  # Uploaded product images (runtime data)
└── telBot/                # Telegram bot (not completed yet)
```

---

## Getting started

### Requirements

- Java 21
- PostgreSQL (with a database named `shop`)
- Node.js 20.19 or later
- A Groq API key (only needed for the AI assistant)

### 1. Create the database

```sql
CREATE DATABASE shop;
```

Tables are created automatically on first start (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Set the backend environment variables

PowerShell (Windows):

```powershell
$env:DB_PASSWORD = "your-postgres-password"
$env:JWT_SECRET = "a-random-secret-at-least-32-characters-long"
$env:GROQ_API_URL = "https://api.groq.com/openai/v1"
$env:GROQ_API_KEY = "your-groq-api-key"
```

Bash (macOS / Linux):

```bash
export DB_PASSWORD="your-postgres-password"
export JWT_SECRET="a-random-secret-at-least-32-characters-long"
export GROQ_API_URL="https://api.groq.com/openai/v1"
export GROQ_API_KEY="your-groq-api-key"
```

### 3. Run the backend

```bash
cd mainServer/javaServer
./mvnw spring-boot:run        # on Windows: .\mvnw.cmd spring-boot:run
```

The API starts on <http://localhost:8080>.

### 4. Run the frontend

```bash
cd mainServer/frontend
npm install
npm run dev
```

Open <http://127.0.0.1:5173>. The dev server proxies `/backend/*` to the Java API.

### 5. Create an administrator

Registration never creates admins. Register a normal account first, then promote it directly in the database:

```sql
UPDATE users SET is_admin = true WHERE username = 'your-username';
```

Log in again afterwards so the new token carries the admin role.

---

## Configuration

### Backend (`application.properties`)

| Property / variable | Description | Default |
| --- | --- | --- |
| `DB_PASSWORD` | PostgreSQL password for user `postgres` | required |
| `JWT_SECRET` | HMAC secret for signing tokens (at least 32 bytes) | required |
| `GROQ_API_URL` | Base URL of the OpenAI-compatible API | required |
| `GROQ_API_KEY` | API key for the AI provider | required |
| `SHOP_UPLOAD_DIRECTORY` | Where uploaded product images are stored | `uploads/products` |
| `spring.servlet.multipart.max-file-size` | Upload size limit | `5MB` |

Never commit secrets. They are read from environment variables on purpose.

### Frontend (`mainServer/frontend/.env`)

Copy `.env.example` to `.env` and adjust if needed:

| Variable | Description |
| --- | --- |
| `BACKEND_URL` | Target of the dev/preview proxy (default `http://localhost:8080`) |
| `VITE_API_BASE_URL` | Optional production API origin; leave unset when using a same-origin reverse proxy |

CORS currently allows `localhost` / `127.0.0.1` on ports `5173` (dev), `4173` (preview), and `5500`.

---

## API overview

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| POST | `/auth/register` | Public | Create an account and receive a token |
| POST | `/auth/login` | Public | Log in and receive a token |
| GET | `/auth/profile` | User | Current user's profile |
| DELETE | `/auth/delete` | User | Delete the current account |
| GET | `/auth/users` | Admin | List all users |
| GET | `/product/products` | Public | List all products |
| GET | `/product/search/{key}` | Public | Search by product name or tag |
| POST | `/product/add` | Admin | Create a product |
| DELETE | `/product/delete/{id}` | Admin | Delete a product |
| POST | `/product/images` | Admin | Upload a product image (multipart `file`) |
| GET | `/product/images/{filename}` | Public | Serve an uploaded image |
| POST | `/cart/items` | User | Add or change an item (`productId`, `quantity`; negative quantity reduces) |
| GET | `/cart/get` | User | Get the current user's cart |
| DELETE | `/cart/delete` | User | Clear the cart |
| POST | `/api/bot/multifile` | Public (currently) | Send a message to the AI assistant (multipart) |

Authenticated requests send the header `Authorization: Bearer <token>`.

---

## Running the tests

```bash
# Backend (CORS and image upload tests)
cd mainServer/javaServer
./mvnw test

# Frontend (utility tests: formatting, discounts, cart totals)
cd mainServer/frontend
npm test
```

The frontend tests are unit tests, not end-to-end tests.

---

## Production notes

- Build the frontend with `npm run build` and serve the `dist/` folder over HTTPS.
- Reverse-proxy requests beginning with `/backend` to the Java API (removing the prefix), or set `VITE_API_BASE_URL` before building and add your production origin to the backend CORS configuration.
- Set `SHOP_UPLOAD_DIRECTORY` to a stable absolute path with write access and back it up.
- Do not expose database credentials or AI keys in the frontend bundle.
- The frontend stores the session in browser `sessionStorage`. Use HTTPS and a restrictive Content Security Policy.

---

## Project status

| Component | Status |
| --- | --- |
| Backend API (auth, products, search, images, cart) | Working |
| Web frontend | Working |
| AI chat endpoint | Basic (text only) |
| Telegram bot | 🚧 **Not completed yet** |

---

## Known limitations

- No checkout, payment, orders, product editing, profile editing, or chat-history retrieval yet.
- The AI endpoint `/api/bot/**` is public. Add authentication, rate limiting, and abuse controls before deploying publicly.
- The AI receives only text and the system prompt. Images are not analyzed or stored, and each request is independent (no conversation memory on the server).
- Deleting a user or product that is referenced by a cart may fail because of foreign-key constraints.
- Deleting a product does not delete its image file.
- The currency is undefined; prices are in neutral whole units, and discounts are percentages (0–100).
- End-to-end behavior against a fully configured live backend (database, auth, cart, AI, uploads) has not been verified.

---

## License

No license has been chosen yet.
