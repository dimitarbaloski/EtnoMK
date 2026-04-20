import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import { regionsApi } from "../../api/api";

export default function CreateRegion() {
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await regionsApi.create(name);
      navigate("/admin/regions");
    } catch {
      setError("Failed to create region.");
    }
  };

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/admin/dashboard">Admin</Link> / <Link to="/admin/regions">Manage Regions</Link> / Create
      </div>
      <h2>Create New Region</h2>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="form-container" style={{ maxWidth: "600px" }}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Region Name</label>
            <input type="text" id="name" value={name} onChange={(e) => setName(e.target.value)}
              required placeholder="e.g., Polog, Pelagonia, Vardar" />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">Create Region</button>
            <Link to="/admin/regions" className="btn-back">Cancel</Link>
          </div>
        </form>
      </div>
    </Layout>
  );
}