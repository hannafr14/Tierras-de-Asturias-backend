# Authentication and Security

## Current Mechanism

Spring Security uses HTTP sessions and cookies. JWTs are not issued. Passwords are stored using BCrypt and are never included in account responses.

`CustomUserDetailsService` assigns the `CUSTOMER` role through `.roles("CUSTOMER")`, producing the `ROLE_CUSTOMER` authority. The user entity does not include persistent role administration, and no administrator panel is implemented.

## Login and Logout

Login authenticates the user, applies a session-ID change strategy, and saves the security context. Its authentication strategy also invalidates the previous CSRF token.

Logout is handled in the security filter chain through POST `/api/auth/logout`: it invalidates the session, clears authentication and the session cookie, and involves the remember-me service in logout processing. The configured response is 204.

An ordinary session has a thirty-minute inactivity timeout. Session cookies currently use HttpOnly and SameSite=Lax.

## Remember-Me Login

The implementation adapts JSON login to Spring Security's persistent remember-me mechanism. The option is received as `rememberMe`; its tokens are stored in `persistent_logins`.

Configured validity is 604800 seconds, or seven days. `REMEMBER_ME_KEY` must be stable and private for the environment. It must not appear in Git, real-value examples, screenshots, or reports.

Persisting users and tokens does not automatically persist all ordinary HTTP sessions across backend restarts.

## CSRF

`HttpSessionCsrfTokenRepository` is used. The `/api/auth/csrf` endpoint returns the header name and token for the current session.

Guest access does not exempt a public endpoint from CSRF protection. Registration, login, and order creation require the token and corresponding cookies. Fetch a new token after authentication.

Do not disable CSRF to resolve a 403. First check the host, cookies, and header.

## Order Access

History uses the identity from `Principal`. Details are queried by both order ID and owner email, so a user cannot choose an arbitrary `userId` to retrieve another customer's orders.

Frontend route guards assist navigation; they are not the authorization boundary. Backend checks remain necessary.

## Before Commercial Use

- Recalculate all order prices and amounts on the server; do not trust the client request body.
- Validate products, availability, and stock with concurrency controls.
- Configure HTTPS, secure cookies, and allowed origins for the chosen deployment.
- Replace development credentials and manage secrets outside the repository.
- Add attempt limits, auditing, and password recovery with temporary tokens.
- Review data normalization, validation, consistent errors, and size limits.
- Define privacy, consent, and retention policies; the frontend checkbox currently stores no evidence of acceptance.
- Prevent logs, backups, and screenshots from exposing personal information.

These are known limitations and outstanding tasks, not security guarantees already implemented.
