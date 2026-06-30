import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { authApi } from "../../api/api";

export default function Register() {
  const [form, setForm] = useState({ username: "", email: "", password: "", confirmPassword: "" });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    try {
      const data = await authApi.register(form);
      if (data.message && !data.error) {
        setSuccess("Registration successful! Redirecting to login...");
        setTimeout(() => navigate("/login"), 2000);
      } else {
        setError(data.message || "Registration failed.");
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
        <h2 style={{ textAlign: "center", marginBottom: "1.5rem" }}>Register for EtnoMK</h2>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input type="text" id="username" name="username" value={form.username} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input type="email" id="email" name="email" value={form.email} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input type="password" id="password" name="password" value={form.password} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <input type="password" id="confirmPassword" name="confirmPassword" value={form.confirmPassword} onChange={handleChange} required />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: "100%", marginTop: "0.5rem" }}>
            Register
          </button>
        </form>

        <p style={{ textAlign: "center", marginTop: "1rem" }}>
          Already have an account? <Link to="/login">Login here</Link>
        </p>
      </div>
    </div>
  );
}