import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../components/Layout";
import { recordsApi } from "../../api/api";

export default function ManageRecords() {
  const [records, setRecords] = useState([]);

  useEffect(() => {
    recordsApi.getAll().then(setRecords).catch(() => setRecords([]));
  }, []);

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure?")) return;
    await recordsApi.delete(id);
    setRecords(records.filter((r) => r.recordId !== id));
  };

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/admin/dashboard">Admin</Link> / Manage Records
      </div>

      <h2>Manage Records</h2>

      <Link to="/admin/dashboard" className="btn-back" style={{ marginBottom: "1.5rem", display: "inline-block" }}>
        ← Back to Dashboard
      </Link>

      {records.length === 0 ? (
        <div className="no-records"><p>No records found.</p></div>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Title</th>
                <th>Region</th>
                <th>Category</th>
                <th>Date Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {records.map((record) => (
                <tr key={record.recordId}>
                  <td><div className="truncate">{record.title}</div></td>
                  <td>{record.region?.name}</td>
                  <td>{record.category?.name}</td>
                  <td>{record.dateCreated}</td>
                  <td>
                    <div className="action-buttons">
                      <Link to={`/admin/records/edit/${record.recordId}`} className="btn btn-edit">Edit</Link>
                      <button className="btn btn-delete" onClick={() => handleDelete(record.recordId)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}