import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../components/Layout";
import { adminApi } from "../../api/api";

export default function AdminDashboard() {
  const [stats, setStats] = useState({ totalRecords: 0, totalRegions: 0, totalCategories: 0 });

  useEffect(() => {
    adminApi.getDashboardStats().then(setStats).catch(() => {});
  }, []);

  const menuItems = [
    { icon: "📋", title: "Records", desc: "Manage, edit, and delete all records in the system", link: "/admin/records", label: "Manage Records" },
    { icon: "🗺️", title: "Regions", desc: "Add, edit, and delete regions for categorization", link: "/admin/regions", label: "Manage Regions" },
    { icon: "📂", title: "Categories", desc: "Manage categories for traditional items", link: "/admin/categories", label: "Manage Categories" },
    { icon: "🧵", title: "Materials", desc: "Manage materials used in traditional items", link: "/admin/materials", label: "Manage Materials" },
    { icon: "🪡", title: "Techniques", desc: "Manage crafting techniques for traditional items", link: "/admin/techniques", label: "Manage Techniques" },
  ];

  return (
    <Layout>
      <div className="breadcrumb">
        <Link to="/">Home</Link> / Admin Dashboard
      </div>

      <h2>🔧 Admin Dashboard</h2>

      <div style={{
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
        gap: "1.5rem",
        marginBottom: "3rem"
      }}>
        {[
          { label: "Total Records", value: stats.totalRecords },
          { label: "Regions", value: stats.totalRegions },
          { label: "Categories", value: stats.totalCategories },
        ].map((s) => (
          <div key={s.label} style={{
            background: "#F5E6C8", padding: "1.5rem",
            borderRadius: "12px", textAlign: "center"
          }}>
            <div style={{ fontSize: "2.5rem", color: "#7A1C1C", fontWeight: "bold" }}>{s.value}</div>
            <div style={{ color: "#666", marginTop: "0.5rem" }}>{s.label}</div>
          </div>
        ))}
      </div>

      <p style={{ color: "#5C1A1A", marginBottom: "2rem", fontSize: "1.3rem", fontWeight: "600" }}>
        Management Options
      </p>

      <div style={{
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
        gap: "2rem",
        marginBottom: "3rem"
      }}>
        {menuItems.map((item) => (
          <div key={item.title} style={{
            background: "white",
            border: "1px solid #e8d8b8",
            borderRadius: "12px",
            padding: "2rem",
            textAlign: "center",
            boxShadow: "0 2px 6px rgba(0,0,0,0.07)",
            transition: "transform 0.3s, box-shadow 0.3s"
          }}>
            <h3 style={{ color: "#5C1A1A", marginBottom: "1rem", fontSize: "1.3rem" }}>
              {item.icon} {item.title}
            </h3>
            <p style={{ color: "#666", marginBottom: "1.5rem", fontSize: "0.95rem" }}>{item.desc}</p>
            <Link to={item.link} className="btn btn-primary">{item.label}</Link>
          </div>
        ))}
      </div>
    </Layout>
  );
}