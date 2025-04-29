import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { cn } from "@/lib/utils";

const Navbar = () => {
  const { currentUser, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <nav className="bg-sjsu-blue text-white shadow-md">
      <div className="container mx-auto px-4 py-3">
        <div className="flex justify-between items-center">
          <Link to="/" className="flex items-center gap-2">
            <span className="text-xl font-bold text-sjsu-gold">SJSU</span>
            <span className="text-xl font-bold">Event Nexus</span>
          </Link>

          <div className="md:hidden">
            <button
              onClick={toggleMenu}
              className="text-white focus:outline-none"
              aria-label="Toggle menu"
            >
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                {isMenuOpen ? (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                ) : (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                )}
              </svg>
            </button>
          </div>

          <div className="hidden md:flex items-center space-x-6">
            <Link
              to="/events"
              className="text-white hover:text-sjsu-gold transition-colors"
            >
              Events
            </Link>

            {isAuthenticated ? (
              <>
                <Link
                  to="/profile"
                  className="text-white hover:text-sjsu-gold transition-colors"
                >
                  Profile
                </Link>
                {currentUser?.role === "organizer" && (
                  <Link
                    to="/create-event"
                    className="text-white hover:text-sjsu-gold transition-colors"
                  >
                    Create Event
                  </Link>
                )}
                <Button
                  variant="secondary"
                  className="bg-sjsu-gold text-black hover:bg-yellow-400"
                  onClick={handleLogout}
                >
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Link to="/login">
                  <Button
                    variant="ghost"
                    className="text-white hover:text-sjsu-gold"
                  >
                    Login
                  </Button>
                </Link>
                <Link to="/register">
                  <Button
                    variant="secondary"
                    className="bg-sjsu-gold text-black hover:bg-yellow-400"
                  >
                    Register
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>

        <div
          className={cn(
            "md:hidden transition-all duration-300 ease-in-out",
            isMenuOpen
              ? "max-h-screen opacity-100 py-4"
              : "max-h-0 opacity-0 overflow-hidden py-0"
          )}
        >
          <div className="flex flex-col space-y-4">
            <Link
              to="/events"
              className="text-white hover:text-sjsu-gold transition-colors"
              onClick={() => setIsMenuOpen(false)}
            >
              Events
            </Link>

            {isAuthenticated ? (
              <>
                <Link
                  to="/profile"
                  className="text-white hover:text-sjsu-gold transition-colors"
                  onClick={() => setIsMenuOpen(false)}
                >
                  Profile
                </Link>
                {currentUser?.role === "organizer" && (
                  <Link
                    to="/create-event"
                    className="text-white hover:text-sjsu-gold transition-colors"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Create Event
                  </Link>
                )}
                <Button
                  variant="secondary"
                  className="bg-sjsu-gold text-black hover:bg-yellow-400 w-full"
                  onClick={() => {
                    handleLogout();
                    setIsMenuOpen(false);
                  }}
                >
                  Logout
                </Button>
              </>
            ) : (
              <div className="flex flex-col space-y-2">
                <Link
                  to="/login"
                  onClick={() => setIsMenuOpen(false)}
                  className="w-full"
                >
                  <Button
                    variant="ghost"
                    className="text-white hover:text-sjsu-gold w-full"
                  >
                    Login
                  </Button>
                </Link>
                <Link
                  to="/register"
                  onClick={() => setIsMenuOpen(false)}
                  className="w-full"
                >
                  <Button
                    variant="secondary"
                    className="bg-sjsu-gold text-black hover:bg-yellow-400 w-full"
                  >
                    Register
                  </Button>
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
