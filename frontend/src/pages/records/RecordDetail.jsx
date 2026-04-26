import { useEffect, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Layout from "../../components/Layout";
import { recordsApi } from "../../api/api";

// ─── Small reusable card for similar results ─────────────────────────────────
function SimilarCard({ record }) {
  const navigate = useNavigate();
  return (
    <div
      onClick={() => navigate(`/records/${record.recordId}`)}
      style={{
        cursor: "pointer",
        borderRadius: "8px",
        overflow: "hidden",
        background: "#fff",
        boxShadow: "0 2px 8px rgba(0,0,0,0.10)",
        transition: "transform 0.15s, box-shadow 0.15s",
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = "translateY(-3px)";
        e.currentTarget.style.boxShadow = "0 6px 16px rgba(0,0,0,0.15)";
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = "translateY(0)";
        e.currentTarget.style.boxShadow = "0 2px 8px rgba(0,0,0,0.10)";
      }}
    >
      <div style={{ width: "100%", height: "140px", background: "#f0e8d8", overflow: "hidden" }}>
        {record.images && record.images.length > 0 ? (
          <img
            src={record.images[0].imagePath}
            alt={record.title}
            style={{ width: "100%", height: "100%", objectFit: "cover" }}
          />
        ) : (
          <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100%", color: "#aaa", fontSize: "0.85rem" }}>
            No Image
          </div>
        )}
      </div>
      <div style={{ padding: "0.75rem" }}>
        <div style={{ fontWeight: "600", color: "#5C1A1A", fontSize: "0.9rem", marginBottom: "0.25rem" }}>
          {record.title}
        </div>
        {record.category && (
          <div style={{ fontSize: "0.78rem", color: "#888" }}>{record.category.name}</div>
        )}
        {record.region && (
          <div style={{ fontSize: "0.78rem", color: "#888" }}>{record.region.name}</div>
        )}
      </div>
    </div>
  );
}

// ─── Similar results panel ────────────────────────────────────────────────────
function SimilarPanel({ recordId, onClose }) {
  const [mode, setMode] = useState("record"); // "record" | "upload"
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [uploadFile, setUploadFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const fileInputRef = useRef();

  // Auto-search by record's own image when panel opens
  useEffect(() => {
    if (mode === "record") {
      setLoading(true);
      setError(null);
      setResults(null);
      recordsApi
        .getSimilar(recordId)
        .then((data) => {
          if (Array.isArray(data)) setResults(data);
          else setError("Unexpected response from server.");
        })
        .catch(() => setError("Could not reach the server."))
        .finally(() => setLoading(false));
    }
  }, [mode, recordId]);

  function handleFileChange(e) {
    const file = e.target.files[0];
    if (!file) return;
    setUploadFile(file);
    setPreview(URL.createObjectURL(file));
    setResults(null);
    setError(null);
  }

  function handleUploadSearch() {
    if (!uploadFile) return;
    setLoading(true);
    setError(null);
    setResults(null);
    recordsApi
      .getSimilarByImage(uploadFile)
      .then((data) => {
        if (Array.isArray(data)) setResults(data);
        else setError(data.error || "Unexpected response.");
      })
      .catch(() => setError("Could not reach the server."))
      .finally(() => setLoading(false));
  }

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,0.45)",
        zIndex: 1000,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "1rem",
      }}
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div
        style={{
          background: "#FAF0DC",
          borderRadius: "12px",
          width: "100%",
          maxWidth: "780px",
          maxHeight: "90vh",
          overflowY: "auto",
          boxShadow: "0 20px 60px rgba(0,0,0,0.3)",
        }}
      >
        {/* Header */}
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "1.25rem 1.5rem",
            borderBottom: "2px solid #e0c89a",
            background: "#F5E6C8",
            borderRadius: "12px 12px 0 0",
          }}
        >
          <h3 style={{ color: "#5C1A1A", margin: 0 }}>🔍 Similar Patterns</h3>
          <button
            onClick={onClose}
            style={{
              background: "none",
              border: "none",
              fontSize: "1.4rem",
              cursor: "pointer",
              color: "#5C1A1A",
              lineHeight: 1,
            }}
          >
            ×
          </button>
        </div>

        {/* Mode tabs */}
        <div style={{ display: "flex", borderBottom: "1px solid #e0c89a", background: "#F5E6C8" }}>
          {[
            { key: "record", label: "Use this record's image" },
            { key: "upload", label: "Upload a pattern" },
          ].map(({ key, label }) => (
            <button
              key={key}
              onClick={() => { setMode(key); setResults(null); setError(null); }}
              style={{
                flex: 1,
                padding: "0.75rem",
                border: "none",
                borderBottom: mode === key ? "3px solid #7A1C1C" : "3px solid transparent",
                background: "transparent",
                color: mode === key ? "#7A1C1C" : "#888",
                fontWeight: mode === key ? "700" : "500",
                cursor: "pointer",
                fontSize: "0.9rem",
                transition: "color 0.15s",
              }}
            >
              {label}
            </button>
          ))}
        </div>

        <div style={{ padding: "1.5rem" }}>
          {/* Upload mode UI */}
          {mode === "upload" && (
            <div style={{ marginBottom: "1.25rem" }}>
              <div
                onClick={() => fileInputRef.current.click()}
                style={{
                  border: "2px dashed #c9a96e",
                  borderRadius: "8px",
                  padding: "2rem",
                  textAlign: "center",
                  cursor: "pointer",
                  background: "#fff",
                  marginBottom: "1rem",
                  transition: "background 0.15s",
                }}
                onMouseEnter={(e) => (e.currentTarget.style.background = "#fdf5e0")}
                onMouseLeave={(e) => (e.currentTarget.style.background = "#fff")}
              >
                {preview ? (
                  <img src={preview} alt="preview" style={{ maxHeight: "180px", maxWidth: "100%", borderRadius: "6px" }} />
                ) : (
                  <>
                    <div style={{ fontSize: "2.5rem", marginBottom: "0.5rem" }}>🖼️</div>
                    <p style={{ color: "#888", margin: 0 }}>Click to select an image file</p>
                    <p style={{ color: "#aaa", fontSize: "0.8rem", marginTop: "0.25rem" }}>JPG, PNG, WEBP</p>
                  </>
                )}
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                style={{ display: "none" }}
                onChange={handleFileChange}
              />
              <button
                onClick={handleUploadSearch}
                disabled={!uploadFile || loading}
                className="btn btn-primary"
                style={{ width: "100%", opacity: !uploadFile || loading ? 0.6 : 1 }}
              >
                {loading ? "Searching…" : "Find Similar Records"}
              </button>
            </div>
          )}

          {/* Loading */}
          {loading && (
            <div style={{ textAlign: "center", padding: "2rem", color: "#888" }}>
              <div style={{ fontSize: "2rem", marginBottom: "0.5rem" }}>⏳</div>
              <p>Running pattern similarity search…</p>
            </div>
          )}

          {/* Error */}
          {error && !loading && (
            <div style={{ background: "#fde8e8", border: "1px solid #f5c6c6", borderRadius: "8px", padding: "1rem", color: "#7a1c1c" }}>
              {error}
            </div>
          )}

          {/* Results */}
          {results && !loading && (
            <>
              {results.length === 0 ? (
                <div style={{ textAlign: "center", padding: "2rem", color: "#888" }}>
                  <div style={{ fontSize: "2rem", marginBottom: "0.5rem" }}>🧵</div>
                  <p>No similar records found. Try adding more records with images.</p>
                </div>
              ) : (
                <>
                  <p style={{ color: "#666", marginBottom: "1rem", fontSize: "0.9rem" }}>
                    Found <strong>{results.length}</strong> visually similar record{results.length !== 1 ? "s" : ""}:
                  </p>
                  <div
                    style={{
                      display: "grid",
                      gridTemplateColumns: "repeat(auto-fill, minmax(160px, 1fr))",
                      gap: "1rem",
                    }}
                  >
                    {results.map((r) => (
                      <SimilarCard key={r.recordId} record={r} />
                    ))}
                  </div>
                </>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── Main RecordDetail page ───────────────────────────────────────────────────
export default function RecordDetail() {
  const { id } = useParams();
  const [record, setRecord] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const [showSimilar, setShowSimilar] = useState(false);

  useEffect(() => {
    recordsApi
      .getById(id)
      .then((data) => {
        if (data && data.recordId) setRecord(data);
        else setNotFound(true);
      })
      .catch(() => setNotFound(true));
  }, [id]);

  if (notFound) {
    return (
      <Layout>
        <div className="breadcrumb">
          <Link to="/">Home</Link> / Record Not Found
        </div>
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
      {showSimilar && (
        <SimilarPanel recordId={id} onClose={() => setShowSimilar(false)} />
      )}

      <div className="breadcrumb">
        <Link to="/">Home</Link> / <Link to="/records/view">Browse Records</Link> / {record.title}
      </div>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr 1fr",
          gap: "2rem",
          marginBottom: "2rem",
        }}
      >
        {/* Image column */}
        <div>
          <div
            style={{
              width: "100%",
              height: "400px",
              background: "#f0e8d8",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              borderRadius: "8px",
              overflow: "hidden",
              marginBottom: "1rem",
            }}
          >
            {record.images && record.images.length > 0 ? (
              <img
                src={record.images[0].imagePath}
                alt={record.title}
                style={{ width: "100%", height: "100%", objectFit: "cover" }}
              />
            ) : (
              <span style={{ color: "#999" }}>No Image</span>
            )}
          </div>

          {/* Search Similar button lives right under the image */}
          <button
            onClick={() => setShowSimilar(true)}
            style={{
              width: "100%",
              padding: "0.75rem 1.5rem",
              background: "linear-gradient(135deg, #7A1C1C, #5C1A1A)",
              color: "#fff",
              border: "none",
              borderRadius: "8px",
              fontSize: "1rem",
              fontWeight: "600",
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: "0.5rem",
              boxShadow: "0 4px 12px rgba(92,26,26,0.3)",
              transition: "opacity 0.2s, transform 0.2s",
            }}
            onMouseEnter={(e) => { e.currentTarget.style.opacity = "0.9"; e.currentTarget.style.transform = "translateY(-1px)"; }}
            onMouseLeave={(e) => { e.currentTarget.style.opacity = "1"; e.currentTarget.style.transform = "translateY(0)"; }}
          >
            🔍 Search Similar
          </button>
        </div>

        {/* Info column */}
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

          <Link
            to="/records/view"
            className="btn btn-primary"
            style={{ marginTop: "1.5rem", display: "inline-block" }}
          >
            Back to Records
          </Link>
        </div>
      </div>
    </Layout>
  );
}