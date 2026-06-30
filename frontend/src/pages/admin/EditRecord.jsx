import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Layout from "../../components/Layout";
import { recordsApi, regionsApi, categoriesApi, materialsApi, techniquesApi } from "../../api/api";

export default function EditRecord() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [record, setRecord] = useState(null);
  const [regions, setRegions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [techniques, setTechniques] = useState([]);
  const [form, setForm] = useState({});
  const [error, setError] = useState("");

  useEffect(() => {
    recordsApi.getById(id).then((data) => { setRecord(data); setForm(data); }).catch(() => {});
    regionsApi.getAll().then(setRegions).catch(() => {});
    categoriesApi.getAll().then(setCategories).catch(() => {});
    materialsApi.getAll().then(setMaterials).catch(() => {});
    techniquesApi.getAll().then(setTechniques).catch(() => {});
  }, [id]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      await recordsApi.update(id, {
        title: form.title,
        description: form.description,
        regionId: form.regionId,
        categoryId: form.categoryId,
        materialId: form.materialId || null,
        techniqueId: form.techniqueId || null,
      });
      navigate("/admin/records");
    } catch {
      setError("Failed to update record.");
    }
  };

  if (!record) return <Layout><p style={{ padding: "2rem" }}>Loading...</p></Layout>;

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/admin/dashboard">Admin</Link> / <Link to="/admin/records">Manage Records</Link> / Edit
      </div>

      <h2>Edit Record</h2>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="form-container">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="title">Title</label>
            <input type="text" id="title" name="title" value={form.title || ""} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea id="description" name="description" value={form.description || ""} onChange={handleChange} required style={{ minHeight: "150px" }} />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="regionId">Region</label>
              <select id="regionId" name="regionId" value={form.regionId || record.region?.regionId || ""} onChange={handleChange} required>
                {regions.map((r) => <option key={r.regionId} value={r.regionId}>{r.name}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="categoryId">Category</label>
              <select id="categoryId" name="categoryId" value={form.categoryId || record.category?.categoryId || ""} onChange={handleChange} required>
                {categories.map((c) => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="materialId">Material</label>
              <select id="materialId" name="materialId" value={form.materialId || record.material?.materialId || ""} onChange={handleChange}>
                <option value="">Select Material</option>
                {materials.map((m) => <option key={m.materialId} value={m.materialId}>{m.name}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="techniqueId">Technique</label>
              <select id="techniqueId" name="techniqueId" value={form.techniqueId || record.technique?.techniqueId || ""} onChange={handleChange}>
                <option value="">Select Technique</option>
                {techniques.map((t) => <option key={t.techniqueId} value={t.techniqueId}>{t.name}</option>)}
              </select>
            </div>
          </div>

          <div className="form-actions">
            <button type="submit" className="btn btn-primary">Save Changes</button>
            <Link to="/admin/records" className="btn-secondary btn">Cancel</Link>
          </div>
        </form>
      </div>
    </Layout>
  );
}