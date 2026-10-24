# Backend Wiki

Technical documentation for the Tierras de Asturias API, based on the repository's code.

## Contents

- [README and Local Setup](../../README.md)
- [API and Examples](API.md)
- [Architecture and Data Model](Architecture-and-Data.md)
- [Authentication and Security](Security.md)
- [Operations, Testing, and Troubleshooting](Operations-and-Testing.md)
- [Screenshots, Design, and Jira Links](Media-and-Links.md)
- [Frontend Repository](https://github.com/hannafr14/Tierras-de-Asturias-Frontend)

## Responsibilities

The backend publishes the catalog, registers customers, maintains cookie-based authentication, and stores orders and their lines. Order history and details require a session and are restricted to the authenticated user.

The frontend manages interaction and visual state. The database stores the catalog, accounts, orders, and persistent remember-me tokens. The API does not store the cart.

## Delivery Scope

The existence of a route, DTO, or test does not imply a complete commercial workflow. This version does not include real payments, invoicing, stock updates, password recovery, or an administration panel.

The pages are local Markdown files. They have not been published to GitHub Wiki or Confluence, and Jira statuses have not been changed. Supplied design and planning links are listed in [Project Links](Media-and-Links.md).
