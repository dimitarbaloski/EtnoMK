import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import { techniquesApi } from "../../api/api";

export default function CreateMaterial() {
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await techniquesApi.create(name);
      navigate("/admin/techniques");
    } catch {
      setError("Failed to create technique.");
    }
  };

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/admin/dashboard">Admin</Link> / <Link to="/admin/techniques">Manage Techniques</Link> / Create
      </div>
      <h2>Create New Material</h2>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="form-container" style={{ maxWidth: "600px" }}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Material Name</label>
            <input type="text" id="name" value={name} onChange={(e) => setName(e.target.value)}
              required placeholder="e.g., Wool, Cotton, Leather, Wood" />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">Create Material</button>
            <Link to="/admin/techniques" className="btn-back">Cancel</Link>
          </div>
        </form>
      </div>
    </Layout>
  );
}