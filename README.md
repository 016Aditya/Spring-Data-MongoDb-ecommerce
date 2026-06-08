# E-Commerce Frontend

A portfolio and learning project built with React, Vite, and Tailwind CSS v4, following a feature-based frontend architecture inspired by industrial best practices.

## Overview

This project is a frontend e-commerce application designed to practice scalable folder structure, routing, service-layer organization, authentication flow, protected routes, and global error handling.  
It is connected to a Java Spring Boot backend, with JWT Spring Security and OAuth2 planned as part of the authentication flow.

## Tech Stack

- React
- Vite
- Tailwind CSS v4
- React Router
- Axios
- Java Spring Boot backend

## Features

- Feature-based folder structure
- Shared service layer for API communication
- Route guards for private and public routes
- Global error handling layer
- Auth context setup
- Tailwind CSS v4 integration
- Scalable project structure for future JWT and OAuth2 integration.

## Project Structure

```txt
src/
│
├── app/
│   ├── App.jsx
│   ├── providers.jsx
│   └── router.jsx
│
├── assets/
│   ├── images/
│   ├── icons/
│   └── fonts/
│
├── components/
│   ├── common/
│   └── layout/
│
├── features/
│   ├── auth/
│   ├── products/
│   ├── cart/
│   ├── orders/
│   ├── reviews/
│   └── profile/
│
├── routes/
│   ├── AppRoutes.jsx
│   ├── PrivateRoute.jsx
│   ├── PublicRoute.jsx
│   └── paths.js
│
├── services/
│   ├── api.js
│   ├── apiEndpoints.js
│   ├── authService.js
│   ├── productService.js
│   ├── cartService.js
│   ├── orderService.js
│   ├── reviewService.js
│   ├── profileService.js
│   └── index.js
│
├── errors/
│   ├── AppError.js
│   ├── errorMessages.js
│   ├── errorHandler.js
│   ├── ErrorBoundary.jsx
│   ├── ErrorFallback.jsx
│   ├── withErrorBoundary.jsx
│   └── NotFound.jsx
│
├── hooks/
│   ├── useDebounce.js
│   ├── useLoading.js
│   └── useToggle.js
│
├── utils/
│   ├── constants.js
│   ├── currency.js
│   ├── validation.js
│   ├── storage.js
│   ├── formatters.js
│   └── helpers.js
│
├── config/
│   ├── env.js
│   └── appConfig.js
│
├── styles/
│   ├── globals.css
│   ├── variables.css
│   └── utilities.css
│
├── tests/
│   ├── setup.js
│   └── mocks/
│
└── main.jsx
```

This structure keeps global app setup separate from domain features and uses shared folders only for code reused across multiple features.

## Getting Started

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd <your-project-folder>
```

### 2. Install dependencies

```bash
npm install
```

### 3. Create environment file

Create a `.env` file in the project root:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=ShopApp
```

In Vite, client-side environment variables must start with `VITE_` to be exposed in the app.

### 4. Start the development server

```bash
npm run dev
```

### 5. Build for production

```bash
npm run build
```

## Available Scripts

- `npm run dev` — start development server
- `npm run build` — create production build
- `npm run preview` — preview production build locally

## Environment Variables

This project uses Vite environment variables from files placed in the project root.

Example:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=ShopApp
```

Important notes:
- Only variables prefixed with `VITE_` are exposed to frontend code.
- Do not commit your real `.env` file to GitHub.
- Commit `.env.example` instead.

## Git Ignore

Make sure the following are ignored:

```gitignore
node_modules/
dist/
.env
.env.local
.env.*.local
coverage/
*.log
```

This keeps dependencies, build output, and sensitive local configuration out of version control.

## Current Status

This project is under active development.  
Current focus areas include:
- base frontend architecture
- auth flow setup
- service layer integration
- shared UI components
- backend integration with Spring Boot

## Planned Improvements

- JWT authentication with Spring Security
- OAuth2 login flow
- Product filtering and search
- Cart persistence
- Checkout improvements
- Order history and profile management
- Testing coverage
- Better UI polish and responsiveness

## Learning Goals

This project is being built to practice:
- scalable React architecture
- industrial folder structure
- clean service layer design
- protected routing
- global error handling
- frontend-backend integration

## Author

Built by Aditya Nihal Singh as a portfolio and learning project.

## License

N.A
