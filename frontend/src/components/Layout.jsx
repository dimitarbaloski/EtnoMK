import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import logo from "../images/logo.png";

export default function Layout({ children }) {
  const { isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  return (
    <>
      <header>
        <div className="header-content">
          <h1>
            <img src={logo} alt="EtnoMK Logo" />
            EtnoMK
          </h1>
          <nav>
            <ul>
              <li><Link to="/">Home</Link></li>
              <li><Link to="/records/view">Browse Records</Link></li>
              <li><Link to="/contact">Contact</Link></li>
              {isAuthenticated() && (
                <li><Link to="/records/create">Add Record</Link></li>
              )}
              {isAdmin() && (
                <li><Link to="/admin/dashboard">Admin</Link></li>
              )}
              {!isAuthenticated() && (
                <>
                  <li><Link to="/login">Login</Link></li>
                  <li><Link to="/register">Register</Link></li>
                </>
              )}
              {isAuthenticated() && (
                <li>
                  <button className="btn btn-secondary" onClick={handleLogout}>
                    Logout
                  </button>
                </li>
              )}
            </ul>
          </nav>
        </div>
      </header>

      <div className="container">
        {children}
      </div>

      <footer>
        <p>&copy; 2026 EtnoMK - Macedonian Cultural Heritage. All rights reserved.</p>
      </footer>
    </>
  );
}