import { Link } from "@tanstack/react-router";
import { useAuth } from "../hooks/useAuth";

export const IndexPage = () => {
  const { isAuthenticated, logout, user } = useAuth();

  return (
    <div>
      <h2>Home Page</h2>
      {isAuthenticated ? (
        <div>
          <p>Welcome, {user?.username || "User"}!</p>
          <button onClick={logout}>Logout</button>
        </div>
      ) : (
        <div>
          <p>You are not logged in.</p>
          <Link to="/login">Login</Link> |{" "}
          <Link to="/register" params={{}}>
            Register
          </Link>
        </div>
      )}
    </div>
  );
};
