# Gather frontend

A separate React + Vite application for the existing Java shop and AI backend. The UI uses real API responses; no sample inventory or simulated purchases are included.

## Start locally

Requirements: Node.js 20.19 or later, Java 21, PostgreSQL, and valid AI provider credentials.

1. Start PostgreSQL and ensure the shop database exists.
2. Set the backend environment variables: DB_PASSWORD, JWT_SECRET (at least 32 bytes for HS256), GROQ_API_URL, and GROQ_API_KEY. The AI URL should be the provider's OpenAI-compatible base URL, such as https://api.groq.com/openai/v1. These are backend secrets: never place them in frontend variables.
3. In a terminal opened in the Java server folder, run the Maven wrapper's spring-boot:run goal. Restart the backend after applying the included cart fixes.
4. Open another terminal in the frontend folder and run:

```shell
npm install
npm run dev
```

5. Open http://127.0.0.1:5173.

The frontend forwards requests through its development proxy to http://localhost:8080. The proxy preserves the browser Origin header, so backend CORS explicitly allows localhost and 127.0.0.1 on development port 5173 and preview port 4173. Restart the Java backend after changing CORS settings. To change the backend address, copy [.env.example](.env.example) to a local environment file named .env and adjust the backend URL. Restart Vite after changing it.

## Available operations

| Screen | Operations |
| --- | --- |
| Discover | List products, server-side name/tag search, client-side tag filtering and sorting, product details, add to bag |
| Account | Registration with all required user fields, login, logout, profile display, permanent self-account deletion |
| My bag | Load cart, increment/decrement quantities, remove individual products, clear bag, discounted-price subtotal |
| Administration | Create products with descriptions, integer price, percentage discount, uploaded image and tags; delete products; list registered members |
| AI assistant | Text messages, image attachment upload, system prompt, numeric user/chat/message metadata, in-memory conversation |

The admin screen is visible only to accounts whose authentication response marks them as admins. The backend enforces permissions. Registration always creates a non-admin account; an existing administrator must be provisioned outside this frontend. Product management reflects the current catalog search result.

## Build and tests

From a terminal in the frontend folder:

```shell
npm run build
npm test
npm run preview
```

The preview server runs at http://127.0.0.1:4173 and uses the same backend proxy. Unit tests cover tag normalization, role flag handling, safe image URLs, numeric formatting, and cart subtotals. They are not end-to-end tests.

## Production deployment

Deploy the generated static build on an HTTPS web server. Configure that web server to reverse-proxy requests beginning with /backend to the Java backend, removing that prefix. The Vite development proxy is not part of the generated static build. Alternatively set VITE_API_BASE_URL before building and explicitly allow your production frontend origin in the backend CORS configuration. Do not expose database credentials or AI keys in browser bundles.

Sessions are stored in tab-scoped browser session storage, not persistent local storage. This remains accessible to scripts in the page; use HTTPS and a restrictive Content Security Policy. For stronger production authentication, consider a backend-managed HttpOnly-cookie design with appropriate CSRF protection.

## Backend integration changes

- Added [CartResponse.java](../javaServer/src/main/java/assistant/main/javaServer/Dto/CartResponse.java) to return a stable cart structure without user/password fields or cart/item recursion.
- Updated [CartService.java](../javaServer/src/main/java/assistant/main/javaServer/Services/CartService.java) and [CartController.java](../javaServer/src/main/java/assistant/main/javaServer/Controllers/CartController.java) to return the cart DTO, map lazy relationships within transactions, return an empty cart for a new user, and safely clear an absent cart.
- Added a no-argument constructor to [CartItemRequest.java](../javaServer/src/main/java/assistant/main/javaServer/Dto/CartItemRequest.java) for JSON request binding.

## Known backend limits

- No checkout, payment, order, product-update, profile-update, standalone tag-management, or chat-history retrieval endpoints exist. These are not implemented as pretend frontend actions.
- Currency remains undefined, so prices use neutral units. Discounts are percentages from 0 to 100. Final unit prices are rounded to whole units; cards, details, price sorting, and cart subtotals use those discounted prices. Original prices are crossed out for discounted products.
- Product images use an admin-only upload endpoint. Choose a JPEG or PNG up to 5 MB in the product form; the frontend uploads it first and sends the returned server path in the product creation request. Images are decoded and re-encoded, limited to 20 megapixels, and stored under generated filenames. Public image delivery supports guests viewing the catalog. The database stores the path, not file bytes or a temporary browser URL.
- Upload storage defaults to the backend working directory's uploads/products folder. Set SHOP_UPLOAD_DIRECTORY to a stable absolute directory in production, grant the backend write access, and back it up. Uploaded images survive backend restarts when that directory is retained. Product deletion does not remove image files; abandoned uploads need periodic cleanup.
- AI only receives text and the system prompt. The controller reads an image filename but does not send image bytes to the AI or persist the image. Attachments therefore are not visually analyzed. The frontend caps image uploads at 1 MB to fit Spring's default file limit.
- The AI service has no catalog lookup or conversation-context integration. Each request is independent, even though previous messages remain visible on the screen.
- Profile responses do not expose numeric user IDs. The assistant provides an explicit metadata user-ID field; it is not a verified account identity. The bot endpoint is currently public and should receive authentication, rate limiting, and abuse controls before public deployment.
- Chat history remains only while the assistant screen is mounted. Do not treat frontend conversation display as server history retrieval.
- Deleting accounts/products with database relationships may fail due to foreign-key constraints; the UI reports the server failure rather than claiming success.

## Verification

Frontend production build and all six utility tests passed, including percentage discounts, rounding, cart totals, and uploaded image URLs. Java backend compilation and five targeted CORS/image tests passed. Dependency installation reported no known vulnerabilities. Live database/auth/cart/AI/upload end-to-end behavior has not been verified with a running configured backend. Restart the backend after applying upload configuration and security changes.
