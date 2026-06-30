import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import { recordsApi, regionsApi, categoriesApi, materialsApi, techniquesApi } from "../../api/api";

export default function CreateRecord() {
  const [regions, setRegions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [techniques, setTechniques] = useState([]);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    regionsApi.getAll().then(setRegions).catch(() => {});
    categoriesApi.getAll().then(setCategories).catch(() => {});
    materialsApi.getAll().then(setMaterials).catch(() => {});
    techniquesApi.getAll().then(setTechniques).catch(() => {});
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    const formData = new FormData(e.target);
    try {
      const data = await recordsApi.create(formData);
      if (data.recordId) navigate(`/records/${data.recordId}`);
      else setError(data.message || "Failed to create record.");
    } catch {
      setError("Something went wrong. Please try again.");
    }
  };

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/records/view">Browse Records</Link> / Create Record
      </div>

      <h2>Create New Record</h2>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="form-container">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="title">Title <span className="required">*</span></label>
            <input type="text" id="title" name="title" required />
            <div className="help-text">Enter the name of the traditional item</div>
          </div>

          <div className="form-group">
            <label htmlFor="description">Description <span className="required">*</span></label>
            <textarea id="description" name="description" required style={{ minHeight: "150px" }} />
            <div className="help-text">Provide detailed information about the item</div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="regionId">Region <span className="required">*</span></label>
              <select id="regionId" name="regionId" required>
                <option value="">Select Region</option>
                {regions.map((r) => (
                  <option key={r.regionId} value={r.regionId}>{r.name}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="categoryId">Category <span className="required">*</span></label>
              <select id="categoryId" name="categoryId" required>
                <option value="">Select Category</option>
                {categories.map((c) => (
                  <option key={c.categoryId} value={c.categoryId}>{c.name}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="materialId">Material</label>
              <select id="materialId" name="materialId">
                <option value="">Select Material</option>
                {materials.map((m) => (
                  <option key={m.materialId} value={m.materialId}>{m.name}</option>
                ))}
              </select>
              <div className="help-text">Optional: wool, silk, metal, etc.</div>
            </div>
            <div className="form-group">
              <label htmlFor="techniqueId">Technique</label>
              <select id="techniqueId" name="techniqueId">
                <option value="">Select Technique</option>
                {techniques.map((t) => (
                  <option key={t.techniqueId} value={t.techniqueId}>{t.name}</option>
                ))}
              </select>
              <div className="help-text">Optional: weaving, embroidery, etc.</div>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="image">Upload Image</label>
            <input type="file" id="image" name="image" accept="image/*" />
            <div className="help-text">Upload an image of the traditional item</div>
          </div>

          <div className="form-actions">
            <button type="submit" className="btn btn-primary">Create Record</button>
            <Link to="/records/view" className="btn btn-secondary">Cancel</Link>
          </div>
        </form>
      </div>
    </Layout>
  );
}