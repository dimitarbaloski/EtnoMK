import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../components/Layout";
import { materialsApi } from "../../api/api";

export default function ManageMaterials() {
  const [items, setItems] = useState([]);

  useEffect(() => { materialsApi.getAll().then(setItems).catch(() => {}); }, []);

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure?")) return;
    await materialsApi.delete(id);
    setItems(items.filter((i) => i.materialId !== id));
  };

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/admin/dashboard">Admin</Link> / Manage Materials
      </div>
      <h2>Manage Materials</h2>
      <div className="action-buttons" style={{ marginBottom: "2rem" }}>
        <Link to="/admin/dashboard" className="btn-back">← Back to Dashboard</Link>
        <Link to="/admin/materials/create" className="btn-create" style={{ marginLeft: "1rem" }}>+ Create New Material</Link>
      </div>
      {items.length === 0 ? (
        <div className="no-items"><p>No materials found. <Link to="/admin/materials/create">Create one now!</Link></p></div>
      ) : (
        <div className="table-container">
          <table>
            <thead><tr><th>Material Name</th><th>Actions</th></tr></thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.materialId}>
                  <td>{item.name}</td>
                  <td><button className="btn btn-delete" onClick={() => handleDelete(item.materialId)}>Delete</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}