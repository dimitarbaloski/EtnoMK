import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";

// Pages
import Home from "./pages/Home";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import Contact from "./pages/Contact";
import AccessDenied from "./pages/error/AccessDenied";

// Records
import ViewRecords from "./pages/records/ViewRecords";
import RecordDetail from "./pages/records/RecordDetail";
import CreateRecord from "./pages/records/CreateRecord";
import SearchResults from "./pages/records/SearchResults";

// Admin
import AdminDashboard from "./pages/admin/AdminDashboard";
import ManageRecords from "./pages/admin/ManageRecords";
import EditRecord from "./pages/admin/EditRecord";
import ManageCategories from "./pages/admin/ManageCategories";
import CreateCategory from "./pages/admin/CreateCategory";
import ManageRegions from "./pages/admin/ManageRegions";
import CreateRegion from "./pages/admin/CreateRegion";
import ManageMaterials from "./pages/admin/ManageMaterials";
import CreateMaterial from "./pages/admin/CreateMaterial";
import ManageTechniques from "./pages/admin/ManageTechniques";
import CreateTechnique from "./pages/admin/CreateTechnique";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/contact" element={<Contact />} />
          <Route path="/access-denied" element={<AccessDenied />} />

          {/* Records */}
          <Route path="/records/view" element={<ViewRecords />} />
          <Route path="/records/:id" element={<RecordDetail />} />
          <Route path="/records/create" element={<CreateRecord />} />
          <Route path="/records/search" element={<SearchResults />} />

          {/* Admin */}
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/records" element={<ManageRecords />} />
          <Route path="/admin/records/edit/:id" element={<EditRecord />} />
          <Route path="/admin/categories" element={<ManageCategories />} />
          <Route path="/admin/categories/create" element={<CreateCategory />} />
          <Route path="/admin/regions" element={<ManageRegions />} />
          <Route path="/admin/regions/create" element={<CreateRegion />} />
          <Route path="/admin/materials" element={<ManageMaterials />} />
          <Route path="/admin/materials/create" element={<CreateMaterial />} />
          <Route path="/admin/techniques" element={<ManageTechniques />} />
          <Route path="/admin/techniques/create" element={<CreateTechnique />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}