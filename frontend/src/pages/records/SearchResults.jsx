import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Layout from "../../components/Layout";
import { recordsApi } from "../../api/api";

export default function SearchResults() {
  const [searchParams] = useSearchParams();
  const keyword = searchParams.get("keyword") || "";
  const [records, setRecords] = useState([]);

  useEffect(() => {
    if (keyword) {
      recordsApi.search(keyword).then(setRecords).catch(() => setRecords([]));
    }
  }, [keyword]);

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/records/view">Browse Records</Link> / Search Results
      </div>

      <h2>Search Results</h2>

      <div style={{ background: "#F5E6C8", padding: "1rem", borderRadius: "4px", marginBottom: "2rem" }}>
        <p>Search results for: <strong>{keyword}</strong></p>
      </div>

      {records.length === 0 ? (
        <div className="no-records">
          <p>No records found for your search.</p>
          <Link to="/records/view" className="btn btn-primary" style={{ marginTop: "1rem", display: "inline-block" }}>
            Back to All Records
          </Link>
        </div>
      ) : (
        <div className="grid">
          {records.map((record) => (
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
    </Layout>
  );
}