import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import { categoriesApi } from "../../api/api";

export default function CreateCategory() {
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await categoriesApi.create(name);
      navigate("/admin/categories");
    } catch {
      setError("Failed to create category.");
    }
  };

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/admin/dashboard">Admin</Link> / <Link to="/admin/categories">Manage Categories</Link> / Create
      </div>
      <h2>Create New Category</h2>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="form-container" style={{ maxWidth: "600px" }}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Category Name</label>
            <input type="text" id="name" value={name} onChange={(e) => setName(e.target.value)}
              required placeholder="e.g., Costumes, Jewelry, Textiles, Handicrafts" />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">Create Category</button>
            <Link to="/admin/categories" className="btn-back">Cancel</Link>
          </div>
        </form>
      </div>
    </Layout>
  );
}