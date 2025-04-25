import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import {
  createRouter,
  RouterProvider,
  createRootRoute,
  createRoute,
} from "@tanstack/react-router";
import "./index.css";
import { AuthProvider } from "./context/AuthContext";
import { RootComponent } from "./components/RootComponent";

import { IndexPage } from "./routes/index";
import { Login } from "./routes/login";
import { Register } from "./routes/register";

const rootRoute = createRootRoute({ component: RootComponent });

const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/",
  component: IndexPage,
});
const loginRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/login",
  component: Login,
});
const registerRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/register",
  component: Register,
});

const routeTree = rootRoute.addChildren([
  indexRoute,
  loginRoute,
  registerRoute,
]);

const router = createRouter({ routeTree, context: { auth: undefined! } });

declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}

const rootElement = document.getElementById("root")!;
if (!rootElement.innerHTML) {
  const root = createRoot(rootElement);
  root.render(
    <StrictMode>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </StrictMode>
  );
}
