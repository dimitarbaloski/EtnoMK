import { Link } from "react-router-dom";

export default function AccessDenied() {
  return (
    <div className="container">
      <div className="alert alert-error" style={{ marginTop: "3rem" }}>
        <h2>Access Denied</h2>
        <p>You do not have permission to access this page.</p>
      </div>
      <Link to="/" className="btn btn-primary">Go to Home</Link>
    </div>
  );
}