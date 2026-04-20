import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../components/Layout";
import { categoriesApi } from "../../api/api";

export default function ManageCategories() {
  const [items, setItems] = useState([]);

  useEffect(() => { categoriesApi.getAll().then(setItems).catch(() => {}); }, []);

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure?")) return;
    await categoriesApi.delete(id);
    setItems(items.filter((i) => i.categoryId !== id));
  };

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/admin/dashboard">Admin</Link> / Manage Categories
      </div>
      <h2>Manage Categories</h2>
      <div className="action-buttons" style={{ marginBottom: "2rem" }}>
        <Link to="/admin/dashboard" className="btn-back">← Back to Dashboard</Link>
        <Link to="/admin/categories/create" className="btn-create" style={{ marginLeft: "1rem" }}>+ Create New Category</Link>
      </div>
      {items.length === 0 ? (
        <div className="no-items"><p>No categories found. <Link to="/admin/categories/create">Create one now!</Link></p></div>
      ) : (
        <div className="table-container">
          <table>
            <thead><tr><th>Category Name</th><th>Actions</th></tr></thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.categoryId}>
                  <td>{item.name}</td>
                  <td><button className="btn btn-delete" onClick={() => handleDelete(item.categoryId)}>Delete</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}