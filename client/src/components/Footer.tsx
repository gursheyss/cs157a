
import React from "react";

const Footer = () => {
  return (
    <footer className="bg-sjsu-blue text-white py-8 mt-12">
      <div className="container mx-auto px-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <h3 className="font-bold text-lg mb-4 flex items-center gap-2">
              <span className="text-sjsu-gold">SJSU</span> Event Nexus
            </h3>
            <p className="text-sm text-gray-300">
              Connecting SJSU students with campus events and activities.
            </p>
          </div>
          
          <div>
            <h4 className="font-semibold mb-4">Quick Links</h4>
            <ul className="space-y-2 text-sm text-gray-300">
              <li>
                <a href="/events" className="hover:text-sjsu-gold transition-colors">
                  Browse Events
                </a>
              </li>
              <li>
                <a href="/login" className="hover:text-sjsu-gold transition-colors">
                  Login
                </a>
              </li>
              <li>
                <a href="/register" className="hover:text-sjsu-gold transition-colors">
                  Register
                </a>
              </li>
            </ul>
          </div>
          
          <div>
            <h4 className="font-semibold mb-4">Contact</h4>
            <p className="text-sm text-gray-300">
              San Jose State University<br />
              1 Washington Square<br />
              San Jose, CA 95192
            </p>
            <p className="text-sm text-gray-300 mt-2">
              events@sjsu.edu<br />
              (408) 555-1234
            </p>
          </div>
        </div>
        
        <div className="border-t border-gray-700 mt-8 pt-6 text-sm text-gray-300 flex flex-col md:flex-row justify-between items-center">
          <p>&copy; {new Date().getFullYear()} SJSU Event Nexus. All rights reserved.</p>
          <div className="mt-4 md:mt-0 flex space-x-4">
            <a href="#" className="hover:text-sjsu-gold transition-colors">Privacy Policy</a>
            <a href="#" className="hover:text-sjsu-gold transition-colors">Terms of Service</a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
