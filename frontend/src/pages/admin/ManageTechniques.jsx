import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../components/Layout";
import { techniquesApi } from "../../api/api";

export default function ManageTechniques() {
  const [items, setItems] = useState([]);

  useEffect(() => { techniquesApi.getAll().then(setItems).catch(() => {}); }, []);

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure?")) return;
    await techniquesApi.delete(id);
    setItems(items.filter((i) => i.techniqueId !== id));
  };

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/admin/dashboard">Admin</Link> / Manage Techniques
      </div>
      <h2>Manage Techniques</h2>
      <div className="action-buttons" style={{ marginBottom: "2rem" }}>
        <Link to="/admin/dashboard" className="btn-back">← Back to Dashboard</Link>
        <Link to="/admin/techniques/create" className="btn-create" style={{ marginLeft: "1rem" }}>+ Create New Technique</Link>
      </div>
      {items.length === 0 ? (
        <div className="no-items"><p>No techniques found. <Link to="/admin/techniques/create">Create one now!</Link></p></div>
      ) : (
        <div className="table-container">
          <table>
            <thead><tr><th>Technique Name</th><th>Actions</th></tr></thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.techniqueId}>
                  <td>{item.name}</td>
                  <td><button className="btn btn-delete" onClick={() => handleDelete(item.techniqueId)}>Delete</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}