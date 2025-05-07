
import { Button } from "@/components/ui/button";
import { useLocation, useNavigate } from "react-router-dom";
import { useEffect } from "react";

const NotFound = () => {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    console.error(
      "404 Error: User attempted to access non-existent route:",
      location.pathname
    );
  }, [location.pathname]);

  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center bg-gray-50">
      <div className="text-center px-4">
        <h1 className="text-5xl font-bold text-sjsu-blue mb-4">404</h1>
        <p className="text-2xl text-gray-600 mb-6">Oops! Page not found</p>
        <p className="text-gray-500 mb-8 max-w-md mx-auto">
          The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Button
            onClick={() => navigate("/")}
            className="bg-sjsu-blue hover:bg-sjsu-blue/90"
          >
            Go to Homepage
          </Button>
          <Button
            onClick={() => navigate("/events")}
            variant="outline"
          >
            Browse Events
          </Button>
        </div>
      </div>
    </div>
  );
};

export default NotFound;
