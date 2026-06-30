import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { authApi } from "../../api/api";
import { jwtDecode } from "jwt-decode";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const data = await authApi.login(username, password);
      if (data.token) {
        const decoded = jwtDecode(data.token);

        login(
        {
            username: decoded.sub,
            roles: decoded.roles
        },
        data.token
        );
        navigate("/");
      } else {
        setError(data.message || "Invalid username or password.");
      }
    } catch {
      setError("Something went wrong. Please try again.");
    }
  };

  return (
    <div style={{
      background: "linear-gradient(135deg, #7A1C1C 0%, #A0784A 100%)",
      minHeight: "100vh",
      display: "flex",
      alignItems: "center",
      justifyContent: "center"
    }}>
      <div style={{
        background: "white",
        padding: "2rem",
        borderRadius: "12px",
        width: "100%",
        maxWidth: "400px",
        boxShadow: "0 0 20px rgba(0,0,0,0.2)"
      }}>
        <h2 style={{ textAlign: "center", marginBottom: "1.5rem" }}>Login to EtnoMK</h2>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: "100%", marginTop: "0.5rem" }}>
            Login
          </button>
        </form>

        <p style={{ textAlign: "center", marginTop: "1rem" }}>
          Don't have an account? <Link to="/register">Register here</Link>
        </p>
      </div>
    </div>
  );
}