import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../components/Layout";
import { recordsApi } from "../api/api";

export default function Home() {
  const [records, setRecords] = useState([]);

  useEffect(() => {
    recordsApi.getAll(0, 6).then((data) => setRecords(data.content || data)).catch(() => setRecords([]));
  }, []);

  return (
    <Layout>
      <div className="hero" style={{
        backgroundColor: "#F5E6C8",
        padding: "3rem",
        textAlign: "center",
        marginBottom: "3rem",
        borderRadius: "12px"
      }}>
        <h2 style={{ fontSize: "2.5rem", marginBottom: "1rem" }}>Welcome to EtnoMK</h2>
        <p style={{ fontSize: "1.1rem", color: "#666", marginBottom: "2rem" }}>
          Explore the rich cultural heritage and traditional items of Macedonia
        </p>
        <div style={{ display: "flex", gap: "1rem", justifyContent: "center" }}>
          <Link to="/records/view" className="btn btn-primary">Browse Records</Link>
          <Link to="/records/create" className="btn btn-secondary">Add Your Item</Link>
        </div>
      </div>

      <h2 style={{ textAlign: "center" }}>Recent Records</h2>

      {records.length === 0 ? (
        <div className="no-records">
          <p>No records found. Be the first to add one!</p>
        </div>
      ) : (
        <div className="grid">
          {records.slice(0, 6).map((record) => (
            <div className="record-card" key={record.recordId}>
              <div className="record-image">
                {record.images && record.images.length > 0 ? (
                  <img src={record.images[0].imagePath} alt={record.title} />
                ) : (
                  <span>No Image</span>
                )}
              </div>
              <div className="record-content">
                <h3>{record.title}</h3>
                <div className="record-meta">
                  {record.category && <p>{record.category.name}</p>}
                  {record.region && <p>{record.region.name}</p>}
                </div>
                <Link to={`/records/${record.recordId}`}>View Details →</Link>
              </div>
            </div>
          ))}
        </div>
      )}

      <div style={{ textAlign: "center", marginBottom: "3rem", marginTop: "2rem" }}>
        <Link to="/records/view" className="btn btn-primary">View All Records</Link>
      </div>
    </Layout>
  );
}
