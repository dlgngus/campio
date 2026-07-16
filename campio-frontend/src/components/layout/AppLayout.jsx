import { useCallback, useEffect, useState } from "react";
import { Outlet } from "react-router-dom";
import { authApi } from "../../api/authApi.js";
import { AUTH_CHANGE_EVENT, isAuthenticated, setAuthenticated } from "../../app/authSession.js";
import Navbar from "./Navbar.jsx";
import MobileTabBar from "./MobileTabBar.jsx";

export default function AppLayout() {
  const [authenticated, setAuthenticatedState] = useState(() => isAuthenticated());
  const [user, setUser] = useState(null);

  const loadUser = useCallback(async () => {
    try {
      const me = await authApi.me();
      setUser(me);
      setAuthenticated(true);
      setAuthenticatedState(true);
    } catch {
      setUser(null);
      setAuthenticated(false);
      setAuthenticatedState(false);
    }
  }, []);

  useEffect(() => {
    loadUser();
  }, [loadUser]);

  useEffect(() => {
    const handleAuthChange = () => {
      if (isAuthenticated()) loadUser();
      else {
        setUser(null);
        setAuthenticatedState(false);
      }
    };
    window.addEventListener(AUTH_CHANGE_EVENT, handleAuthChange);
    window.addEventListener("storage", handleAuthChange);
    return () => {
      window.removeEventListener(AUTH_CHANGE_EVENT, handleAuthChange);
      window.removeEventListener("storage", handleAuthChange);
    };
  }, [loadUser]);

  return (
    <div className="app-shell">
      <Navbar user={user} authenticated={authenticated} onLoggedOut={() => { setUser(null); setAuthenticatedState(false); }} />
      <main className="app-main">
        <Outlet context={{ user, authenticated }} />
      </main>
      <MobileTabBar authenticated={authenticated} />
    </div>
  );
}
