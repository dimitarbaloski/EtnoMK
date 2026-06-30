import { useEffect, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Layout from "../../components/Layout";
import { recordsApi, regionsApi } from "../../api/api";

function SimilarCard({ record }) {
  const navigate = useNavigate();
  const image = record.images?.[0];

  return (
    <button
      type="button"
      className="similar-result-card"
      onClick={() => navigate(`/records/${record.recordId}`)}
    >
      <div className="similar-result-image">
        {image ? (
          <img src={image.imagePath} alt={record.title} />
        ) : (
          <div className="similar-result-placeholder">No Image</div>
        )}
      </div>

      <div className="similar-result-content">
        <div className="similar-result-title">{record.title}</div>
        {record.region?.name && <div className="similar-result-region">{record.region.name}</div>}
      </div>
    </button>
  );
}

function SimilarPanel({ recordId, record, onClose }) {
  const [mode, setMode] = useState("record");
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [recordSearchKey, setRecordSearchKey] = useState(0);
  const [uploadFile, setUploadFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [regions, setRegions] = useState([]);
  const [selectedRegion, setSelectedRegion] = useState("");
  const fileInputRef = useRef(null);

  useEffect(() => {
    regionsApi.getAll().then(setRegions).catch(() => {});
  }, []);

  useEffect(() => {
    if (mode !== "record") return;

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
  }, [mode, recordId, recordSearchKey]);

  useEffect(() => {
    return () => {
      if (preview) URL.revokeObjectURL(preview);
    };
  }, [preview]);

  function handleFileChange(event) {
    const file = event.target.files?.[0];
    if (!file) return;

    if (preview) URL.revokeObjectURL(preview);
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
      .getSimilarByImage(uploadFile, 20, selectedRegion || null)
      .then((data) => {
        if (Array.isArray(data)) setResults(data);
        else setError(data.error || "Unexpected response.");
      })
      .catch(() => setError("Could not reach the server."))
      .finally(() => setLoading(false));
  }

  const recordImage = record?.images?.[0];

  return (
    <div className="similar-modal-backdrop" onClick={(event) => event.target === event.currentTarget && onClose()}>
      <div className="similar-modal" role="dialog" aria-modal="true" aria-labelledby="similar-title">
        <div className="similar-modal-header">
          <div>
            <h3 id="similar-title">Similar Items</h3>
            <p>Compare the full record image or upload a close visual detail.</p>
          </div>
          <button type="button" className="similar-close-btn" onClick={onClose} aria-label="Close">
            x
          </button>
        </div>

        <div className="similar-tabs">
          <button
            type="button"
            className={mode === "record" ? "similar-tab active" : "similar-tab"}
            onClick={() => setMode("record")}
          >
            Record image
          </button>
          <button
            type="button"
            className={mode === "upload" ? "similar-tab active" : "similar-tab"}
            onClick={() => setMode("upload")}
          >
            Upload image
          </button>
        </div>

        <div className="similar-modal-body">
          {mode === "record" && (
            <div className="similar-mode-panel">
              <div className="similar-record-preview">
                <div className="similar-record-thumb">
                  {recordImage ? <img src={recordImage.imagePath} alt={record.title} /> : <span>No Image</span>}
                </div>
                <div>
                  <h4>Search from this record</h4>
                  <p>{record?.title}</p>
                  {record?.region?.name && <span>{record.region.name}</span>}
                </div>
              </div>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => setRecordSearchKey((key) => key + 1)}
                disabled={loading}
              >
                Refresh Results
              </button>
            </div>
          )}

          {mode === "upload" && (
            <div className="similar-upload-panel">
              <button
                type="button"
                className={preview ? "similar-dropzone has-preview" : "similar-dropzone"}
                onClick={() => fileInputRef.current?.click()}
              >
                {preview ? (
                  <img src={preview} alt="Uploaded image preview" />
                ) : (
                  <span>
                    <strong>Choose an image detail</strong>
                    <span>Upload a close crop or full photo of the object, decoration, shape, material, or detail.</span>
                  </span>
                )}
              </button>

              <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                className="visually-hidden-file"
                accept="image/*"
              />

              <div className="similar-upload-controls">
                <label>
                  Region
                  <select value={selectedRegion} onChange={(event) => setSelectedRegion(event.target.value)}>
                    <option value="">All regions</option>
                    {regions.map((region) => (
                      <option key={region.regionId} value={region.regionId}>
                        {region.name}
                      </option>
                    ))}
                  </select>
                </label>

                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleUploadSearch}
                  disabled={loading || !uploadFile}
                >
                  Search Similar
                </button>
              </div>
            </div>
          )}

          {loading && <div className="similar-state">Searching similar items...</div>}
          {error && <div className="similar-error">{error}</div>}

          {results?.length > 0 && (
            <div className="similar-results-grid">
              {results.map((result) => (
                <SimilarCard key={result.recordId} record={result} />
              ))}
            </div>
          )}

          {results && results.length === 0 && !loading && (
            <div className="similar-empty">No close visual matches were found.</div>
          )}
        </div>
      </div>
    </div>
  );
}

function InfoRow({ label, value }) {
  if (!value) return null;

  return (
    <div className="record-info-row">
      <div className="record-info-label">{label}</div>
      <div className="record-info-value">{value}</div>
    </div>
  );
}

export default function RecordDetail() {
  const { id } = useParams();
  const [record, setRecord] = useState(null);
  const [error, setError] = useState(null);
  const [showSimilar, setShowSimilar] = useState(false);

  useEffect(() => {
    setRecord(null);
    setError(null);

    recordsApi
      .getById(id)
      .then((data) => {
        if (data?.recordId) setRecord(data);
        else setError("Record not found.");
      })
      .catch(() => setError("Could not load this record."));
  }, [id]);

  if (error) {
    return (
      <Layout>
        <div className="container">
          <div className="breadcrumb">
            <Link to="/">Home</Link> / <Link to="/records/view">Browse Records</Link> / Record
          </div>
          <div className="card">
            <h2>Record Not Found</h2>
            <p>{error}</p>
            <Link to="/records/view" className="btn btn-primary record-back-link">
              Back to Records
            </Link>
          </div>
        </div>
      </Layout>
    );
  }

  if (!record) {
    return (
      <Layout>
        <div className="container">Loading...</div>
      </Layout>
    );
  }

  const images = record.images || [];
  const primaryImage = images[0];

  return (
    <Layout>
      {showSimilar && <SimilarPanel recordId={id} record={record} onClose={() => setShowSimilar(false)} />}

      <div className="container">
        <div className="breadcrumb">
          <Link to="/">Home</Link> / <Link to="/records/view">Browse Records</Link> / {record.title}
        </div>

        <div className="record-detail-grid">
          <div className="record-detail-image">
            {primaryImage ? <img src={primaryImage.imagePath} alt={record.title} /> : <span>No Image</span>}
          </div>

          <section>
            <h2 className="record-detail-title">{record.title}</h2>

            <div className="card record-info-card">
              <InfoRow label="Category" value={record.category?.name} />
              <InfoRow label="Region" value={record.region?.name} />
              <InfoRow label="Material" value={record.material?.name} />
              <InfoRow label="Technique" value={record.technique?.name} />
              <InfoRow label="Date Created" value={record.dateCreated} />
            </div>

            {record.description && (
              <div className="card">
                <h3 className="record-section-title">Description</h3>
                <p>{record.description}</p>
              </div>
            )}

            <div className="record-actions">
              <button type="button" className="btn btn-primary" onClick={() => setShowSimilar(true)}>
                Search Similar
              </button>
              <Link to="/records/view" className="btn btn-secondary">
                Back to Records
              </Link>
            </div>
          </section>
        </div>

        {images.length > 1 && (
          <section className="record-gallery">
            <h2>Gallery</h2>
            <div className="grid">
              {images.map((image) => (
                <div className="record-image" key={image.imageId || image.imagePath}>
                  <img src={image.imagePath} alt={record.title} />
                </div>
              ))}
            </div>
          </section>
        )}
      </div>
    </Layout>
  );
}
