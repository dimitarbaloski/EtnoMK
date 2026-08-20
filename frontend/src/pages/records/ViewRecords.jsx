import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import Pagination from "../../components/Pagination";
import { recordsApi, regionsApi, categoriesApi } from "../../api/api";

export default function ViewRecords() {
  const [pageData, setPageData] = useState({ content: [], number: 0, totalPages: 0, totalElements: 0 });
  const [regions, setRegions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [regionId, setRegionId] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [activeFilter, setActiveFilter] = useState({ regionId: "", categoryId: "" });
  const [page, setPage] = useState(0);
  const navigate = useNavigate();
  const pageSize = 9;

  useEffect(() => {
    regionsApi.getAll().then(setRegions).catch(() => setRegions([]));
    categoriesApi.getAll().then(setCategories).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    const request = activeFilter.regionId || activeFilter.categoryId
      ? recordsApi.filter(activeFilter.regionId, activeFilter.categoryId, page, pageSize)
      : recordsApi.getAll(page, pageSize);

    request
      .then(setPageData)
      .catch(() => setPageData({ content: [], number: 0, totalPages: 0, totalElements: 0 }));
  }, [activeFilter, page]);

  const handleSearch = (e) => {
    e.preventDefault();
    if (keyword.trim()) navigate(`/records/search?keyword=${encodeURIComponent(keyword)}`);
  };

  const handleFilter = (e) => {
    e.preventDefault();
    setPage(0);
    setActiveFilter({ regionId, categoryId });
  };

  const records = pageData.content || [];
  const currentPage = pageData.number || 0;
  const totalPages = pageData.totalPages || 0;

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / Browse Records
      </div>

      <h2>Browse Records</h2>

      <div className="filter-section">
        <form onSubmit={handleSearch} style={{ display: "flex", gap: "0.5rem", width: "100%" }}>
          <input
            type="text"
            name="keyword"
            placeholder="Search records..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">🔍</button>
        </form>
      </div>

      <div className="filter-section">
        <form onSubmit={handleFilter} style={{ display: "flex", gap: "1rem", flexWrap: "wrap", width: "100%" }}>
          <select value={regionId} onChange={(e) => setRegionId(e.target.value)}>
            <option value="">Select Region</option>
            {regions.map((r) => (
              <option key={r.regionId} value={r.regionId}>{r.name}</option>
            ))}
          </select>
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
            <option value="">Select Category</option>
            {categories.map((c) => (
              <option key={c.categoryId} value={c.categoryId}>{c.name}</option>
            ))}
          </select>
          <button type="submit" className="btn btn-primary">Filter</button>
        </form>
      </div>

      {records.length === 0 ? (
        <div className="no-records">
          <p>No records found. <Link to="/records/create">Create one now!</Link></p>
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
                  {record.material && <p>Material: {record.material.name}</p>}
                  {record.technique && <p>Technique: {record.technique.name}</p>}
                </div>
                <Link to={`/records/${record.recordId}`}>View Details →</Link>
              </div>
            </div>
          ))}
        </div>
      )}

      <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={setPage} />
    </Layout>
  );
}
