# Smart Assistant

A full-stack online shop with an AI chat assistant. The project consists of a **Spring Boot REST API** (JWT authentication, products, tags, search, image upload, shopping cart, AI proxy), a **React + Vite** storefront ("Gather") that talks to it, and a **Telegram bot** (Python + aiogram) that lets users browse and search products and chat with the AI assistant.

> **Status:** work in progress. The shop backend, web frontend and a first version of the Telegram bot are usable (see [Project status](#project-status) and [Known limitations](#known-limitations)).

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Telegram bot](#telegram-bot)
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
- The same endpoint is used by the Telegram bot

**Frontend**
- Discover page with search, tag filtering, sorting, and product details
- Account, bag, administration, and AI assistant screens
- Light/dark theme toggle

**Telegram bot**
- `/start` shows a welcome message (from `welcomeMessage.txt`), a heart reaction, and a main menu keyboard
- Product list as inline buttons, loaded from the Java API
- Product search through Telegram **inline mode** (`@your_bot search:keyword`) with a popup of results
- For every plain text message the bot offers two buttons: search for it, or send it to the AI assistant
- Photos are forwarded directly to the Java server together with their caption

---

## Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.1, Spring Web MVC, Spring Security, Spring Data JPA |
| Auth | JWT ([jjwt](https://github.com/jwtk/jjwt) 0.11.5), BCrypt |
| Database | PostgreSQL |
| AI | Groq (OpenAI-compatible chat completions API) |
| Frontend | React 19, Vite 6, lucide-react |
| Telegram bot | Python 3.10+, [aiogram](https://docs.aiogram.dev/) 3, aiohttp, aiofiles |
| Build tools | Maven Wrapper, npm, pip |

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
└── telBot/                # Telegram bot (aiogram)
    ├── main.py            # All bot handlers
    └── welcomeMessage.txt # Text sent after /start
```

---

## Getting started

### Requirements

- Java 21
- PostgreSQL (with a database named `shop`)
- Node.js 20.19 or later
- Python 3.10 or later (only for the Telegram bot)
- A Groq API key (only needed for the AI assistant)
- A Telegram bot token from [@BotFather](https://t.me/BotFather) (only for the Telegram bot)

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

### 6. Run the Telegram bot (optional)

See the [Telegram bot](#telegram-bot) section below.

---

## Telegram bot

The bot lives in `telBot/` and talks to the Java API over HTTP. The Java server must be running before the bot is started.

### 1. Create the bot

1. Open [@BotFather](https://t.me/BotFather) and send `/newbot`. Copy the token it gives you.
2. Send `/setinline` to BotFather, choose your bot, and set a placeholder text (for example `Search products...`). **Without this step the inline search does not work.**

### 2. Install the dependencies

```bash
cd telBot
python -m venv .venv
# Windows:  .venv\Scripts\activate
# macOS/Linux:  source .venv/bin/activate
pip install aiogram aiohttp aiofiles
```

### 3. Set the environment variables

PowerShell (Windows):

```powershell
$env:BOT_TOKEN = "your-telegram-bot-token"
$env:JAVA_SERVER = "http://localhost:8080/"
```

Bash (macOS / Linux):

```bash
export BOT_TOKEN="your-telegram-bot-token"
export JAVA_SERVER="http://localhost:8080/"
```

> **Important:** `JAVA_SERVER` must end with a trailing `/`. The bot builds URLs by simple concatenation (`JAVA_SERVER + "product/products"`), so a missing slash produces broken addresses.

### 4. Check the welcome message file

`/start` reads `welcomeMessage.txt` from the **current working directory**, so run the bot from inside `telBot/` and make sure the file exists (UTF-8).

### 5. Run the bot

```bash
python main.py
```

The bot uses long polling, so it does not need a public URL or a webhook.

### How it works

| Trigger | Handler | What happens |
| --- | --- | --- |
| `/start` | `command_start_handler` | Reacts with ❤️, sends the welcome text and the main menu keyboard |
| Text `لیست محصولات` | `show_all_products` | Calls `GET product/products` and shows each product as an inline button |
| Click on a product button (`product:<id>`) | `product_detail` | Fetches the product and sends its name, price and short description |
| Text `جستجو` | `shearch_handler` | Asks the user to send the search keyword |
| Inline query `@bot search:<keyword>` | `inline_search_handler` | Calls `GET product/search/{keyword}` and returns up to 20 results as an inline popup |
| Any other text | `ask_to_search` | Replies with two buttons: **search** (opens inline mode with the text prefilled) and **send to AI** |
| Click on **send to AI** (`send_ai`) | `send_ai_handler` | Finds the original user message (the reply target), deletes the button message, and forwards it to the AI |
| Photo | `photo_handler` | Forwards the photo and its caption to the AI |

**AI forwarding.** `ai_handler` sends a `multipart/form-data` POST to `api/bot/multifile` with these fields:

| Field | Value |
| --- | --- |
| `user_id` | Telegram user ID |
| `chat_id` | Telegram chat ID |
| `message_id` | Telegram message ID |
| `username` | Telegram username |
| `content` | Message text or photo caption |
| `msg_type` | `TEXT` or `PHOTO` |
| `file` | The largest photo size as JPEG (only for photos) |

The `reply` field of the JSON response is sent back to the user.

**Handler order matters.** aiogram runs the first handler whose filter matches. The generic text handler (`F.text`) must stay **after** the specific ones (`/start`, `لیست محصولات`, `جستجو`), otherwise it swallows their messages.

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

CORS currently allows `localhost` / `127.0.0.1` on ports `5173` (dev), `4173` (preview), and `5500`. The Telegram bot is a server-side client, so it is not affected by CORS.

### Telegram bot (environment variables)

| Variable | Description | Default |
| --- | --- | --- |
| `BOT_TOKEN` | Token from BotFather | required |
| `JAVA_SERVER` | Base URL of the Java API, **with a trailing `/`** | required |

Never commit the bot token. If it leaks, revoke it with `/revoke` in BotFather.

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
| POST | `/api/bot/multifile` | Public (currently) | Send a message to the AI assistant (multipart); used by the web frontend and the Telegram bot |

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

The frontend tests are unit tests, not end-to-end tests. The Telegram bot has no automated tests yet.

---

## Production notes

- Build the frontend with `npm run build` and serve the `dist/` folder over HTTPS.
- Reverse-proxy requests beginning with `/backend` to the Java API (removing the prefix), or set `VITE_API_BASE_URL` before building and add your production origin to the backend CORS configuration.
- Set `SHOP_UPLOAD_DIRECTORY` to a stable absolute path with write access and back it up.
- Do not expose database credentials or AI keys in the frontend bundle.
- The frontend stores the session in browser `sessionStorage`. Use HTTPS and a restrictive Content Security Policy.
- Run the Telegram bot as a long-lived process (systemd, Docker, or a process manager) with `BOT_TOKEN` and `JAVA_SERVER` set in its environment.
- If you want product thumbnails in the inline search popup, the image URLs must be public HTTPS addresses reachable from Telegram's servers. `localhost` or private IPs will not work.

---

## Project status

| Component | Status |
| --- | --- |
| Backend API (auth, products, search, images, cart) | Working |
| Web frontend | Working |
| AI chat endpoint | Basic (text only) |
| Telegram bot | 🚧 **First version**: menu, product list, inline search, AI forwarding |

---

## Known limitations

- No checkout, payment, orders, product editing, profile editing, or chat-history retrieval yet.
- The AI endpoint `/api/bot/**` is public. Add authentication, rate limiting, and abuse controls before deploying publicly.
- The AI receives only text and the system prompt. Images are not analyzed or stored, and each request is independent (no conversation memory on the server).
- Deleting a user or product that is referenced by a cart may fail because of foreign-key constraints.
- Deleting a product does not delete its image file.
- The currency is undefined in the API; prices are in neutral whole units, and discounts are percentages (0–100). The bot currently prints prices with the word "تومان" in the inline search results.
- Telegram bot:
  - The product detail button calls `GET product/{id}`, which is not part of the documented API. Add that endpoint to the Java server or change the bot to reuse the product list.
  - Inline search results have no thumbnails yet (`thumbnail_url` is `None`).
  - The bot does not authenticate users against the shop accounts and has no cart or ordering support.
  - Every text message gets the search/AI buttons; there is no conversation memory between AI requests.
  - All handlers live in one file (`main.py`) and there are no tests.
- End-to-end behavior against a fully configured live backend (database, auth, cart, AI, uploads) has not been verified.

---


