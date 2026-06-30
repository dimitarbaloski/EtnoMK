import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../components/Layout";
import { regionsApi } from "../../api/api";

export default function ManageRegions() {
  const [items, setItems] = useState([]);

  useEffect(() => { regionsApi.getAll().then(setItems).catch(() => {}); }, []);

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure?")) return;
    await regionsApi.delete(id);
    setItems(items.filter((i) => i.regionId !== id));
  };

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/admin/dashboard">Admin</Link> / Manage Regions
      </div>
      <h2>Manage Regions</h2>
      <div className="action-buttons" style={{ marginBottom: "2rem" }}>
        <Link to="/admin/dashboard" className="btn-back">← Back to Dashboard</Link>
        <Link to="/admin/regions/create" className="btn-create" style={{ marginLeft: "1rem" }}>+ Create New Region</Link>
      </div>
      {items.length === 0 ? (
        <div className="no-items"><p>No regions found. <Link to="/admin/regions/create">Create one now!</Link></p></div>
      ) : (
        <div className="table-container">
          <table>
            <thead><tr><th>Region Name</th><th>Actions</th></tr></thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.regionId}>
                  <td>{item.name}</td>
                  <td><button className="btn btn-delete" onClick={() => handleDelete(item.regionId)}>Delete</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}