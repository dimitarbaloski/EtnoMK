import { createContext, useContext, useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";

const AuthContext = createContext(null);

function clearStoredAuth() {
  sessionStorage.removeItem("user");
  sessionStorage.removeItem("token");
  localStorage.removeItem("user");
  localStorage.removeItem("token");
}

function isTokenValid(token) {
  if (!token) return false;

  try {
    const decoded = jwtDecode(token);
    return decoded.exp && decoded.exp * 1000 > Date.now();
  } catch {
    return false;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null); // { username, role }
  const [loading, setLoading] = useState(true);

  // Keep auth for the current browser session only, and clear old localStorage logins.
  useEffect(() => {
    localStorage.removeItem("user");
    localStorage.removeItem("token");

    const token = sessionStorage.getItem("token");
    const stored = sessionStorage.getItem("user");

    if (stored && isTokenValid(token)) {
      setUser(JSON.parse(stored));
    } else {
      clearStoredAuth();
    }

    setLoading(false);
  }, []);

  const login = (userData, token) => {
    sessionStorage.setItem("user", JSON.stringify(userData));
    sessionStorage.setItem("token", token);
    setUser(userData);
  };

  const logout = async () => {
    try {
      await fetch("/api/auth/logout", {
        method: "POST",
        headers: { Authorization: `Bearer ${sessionStorage.getItem("token")}` },
      });
    } catch (e) {
      // ignore
    }
    clearStoredAuth();
    setUser(null);
  };

  const isAuthenticated = () => !!user;
  const isAdmin = () => user?.roles?.includes("ROLE_ADMIN");

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated, isAdmin, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
