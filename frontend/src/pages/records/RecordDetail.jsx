import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Layout from "../../components/Layout";
import { recordsApi } from "../../api/api";

export default function RecordDetail() {
  const { id } = useParams();
  const [record, setRecord] = useState(null);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    recordsApi.getById(id)
      .then((data) => {
        if (data && data.recordId) setRecord(data);
        else setNotFound(true);
      })
      .catch(() => setNotFound(true));
  }, [id]);

  if (notFound) {
    return (
      <Layout>
        <div className="breadcrumb"><Link to="/">Home</Link> / Record Not Found</div>
        <h2>Record Not Found</h2>
        <p>The record you're looking for doesn't exist.</p>
        <Link to="/records/view" className="btn btn-primary" style={{ marginTop: "1rem", display: "inline-block" }}>
          Back to Records
        </Link>
      </Layout>
    );
  }

  if (!record) return <Layout><p style={{ padding: "2rem" }}>Loading...</p></Layout>;

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/records/view">Browse Records</Link> / {record.title}
      </div>

      <div style={{
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "2rem",
        marginBottom: "2rem"
      }}>
        <div style={{
          width: "100%",
          height: "400px",
          background: "#f0e8d8",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          borderRadius: "8px",
          overflow: "hidden"
        }}>
          {record.images && record.images.length > 0 ? (
            <img src={record.images[0].imagePath} alt={record.title}
              style={{ width: "100%", height: "100%", objectFit: "cover" }} />
          ) : (
            <span style={{ color: "#999" }}>No Image</span>
          )}
        </div>

        <div>
          <h2 style={{ marginBottom: "1rem" }}>{record.title}</h2>

          {record.category && (
            <div style={{ marginBottom: "1.5rem" }}>
              <div style={{ fontWeight: "600", color: "#5C1A1A", marginBottom: "0.5rem" }}>Category</div>
              <div style={{ color: "#666" }}>{record.category.name}</div>
            </div>
          )}

          {record.region && (
            <div style={{ marginBottom: "1.5rem" }}>
              <div style={{ fontWeight: "600", color: "#5C1A1A", marginBottom: "0.5rem" }}>Region</div>
              <div style={{ color: "#666" }}>{record.region.name}</div>
            </div>
          )}

          {record.material && (
            <div style={{ marginBottom: "1.5rem" }}>
              <div style={{ fontWeight: "600", color: "#5C1A1A", marginBottom: "0.5rem" }}>Material</div>
              <div style={{ color: "#666" }}>{record.material.name}</div>
            </div>
          )}

          {record.technique && (
            <div style={{ marginBottom: "1.5rem" }}>
              <div style={{ fontWeight: "600", color: "#5C1A1A", marginBottom: "0.5rem" }}>Technique</div>
              <div style={{ color: "#666" }}>{record.technique.name}</div>
            </div>
          )}

          <div style={{ marginBottom: "1.5rem" }}>
            <div style={{ fontWeight: "600", color: "#5C1A1A", marginBottom: "0.5rem" }}>Date Created</div>
            <div style={{ color: "#666" }}>{record.dateCreated}</div>
          </div>

          {record.description && (
            <div style={{ background: "#F5E6C8", padding: "1.5rem", borderRadius: "8px", marginTop: "1rem" }}>
              <h3 style={{ color: "#5C1A1A", marginBottom: "1rem" }}>Description</h3>
              <p>{record.description}</p>
            </div>
          )}

          <Link to="/records/view" className="btn btn-primary" style={{ marginTop: "1.5rem", display: "inline-block" }}>
            Back to Records
          </Link>
        </div>
      </div>
    </Layout>
  );
}