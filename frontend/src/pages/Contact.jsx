import { useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../components/Layout";

export default function Contact() {
  const [form, setForm] = useState({ name: "", email: "", message: "" });
  const [success, setSuccess] = useState("");

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = (e) => {
    e.preventDefault();
    // Wire up to your backend if needed
    setSuccess("Message sent successfully!");
    setForm({ name: "", email: "", message: "" });
  };

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / Contact
      </div>

      <h2>Contact Us</h2>

      <div style={{
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "3rem",
        marginBottom: "3rem"
      }}>
        <div style={{ background: "#F5E6C8", padding: "2rem", borderRadius: "8px" }}>
          <h3 style={{ color: "#5C1A1A", marginBottom: "1.5rem" }}>Technical Support</h3>
          <div style={{ marginBottom: "1.5rem" }}>
            <strong style={{ color: "#8B4513" }}>📧 Email:</strong>
            <p style={{ marginTop: "0.5rem" }}>support@etnomk.mk</p>
          </div>
          <div style={{ marginBottom: "1.5rem" }}>
            <strong style={{ color: "#8B4513" }}>📞 Phone:</strong>
            <p style={{ marginTop: "0.5rem" }}>+389 (2) 123 4567</p>
          </div>
          <div style={{ marginBottom: "1.5rem" }}>
            <strong style={{ color: "#8B4513" }}>🕐 Hours:</strong>
            <p style={{ marginTop: "0.5rem" }}>Monday - Friday: 9:00 AM - 5:00 PM</p>
            <p>Saturday - Sunday: Closed</p>
          </div>
          <div style={{ marginBottom: "1.5rem" }}>
            <strong style={{ color: "#8B4513" }}>📍 Address:</strong>
            <p style={{ marginTop: "0.5rem" }}>Skopje, Macedonia</p>
          </div>
        </div>

        <div className="form-container">
          <h3 style={{ color: "#5C1A1A", marginBottom: "1.5rem" }}>Send us a Message</h3>

          {success && <div className="alert alert-success">{success}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="name">Name</label>
              <input type="text" id="name" name="name" value={form.name} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input type="email" id="email" name="email" value={form.email} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label htmlFor="message">Message</label>
              <textarea id="message" name="message" value={form.message} onChange={handleChange} required />
            </div>
            <button type="submit" className="btn btn-primary">Send Message</button>
          </form>
        </div>
      </div>
    </Layout>
  );
}