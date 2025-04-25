import { Outlet, Link } from "@tanstack/react-router";
import { useAuth } from "../hooks/useAuth";

export const RootComponent = () => {
  const { isLoading } = useAuth();
  if (isLoading) {
    return <div>Loading Application...</div>;
  }
  return (
    <>
      <div style={{ display: "flex", gap: "1rem", padding: "1rem" }}>
        <Link to="/">Home</Link>
        <Link to="/login">Login</Link>
        <Link to="/register">Register</Link>
      </div>
      <hr />
      <Outlet />
    </>
  );
};
