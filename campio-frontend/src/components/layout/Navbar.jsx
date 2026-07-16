import { useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { Moon, Sun } from "lucide-react";
import { authApi } from "../../api/authApi.js";
import { AUTH_CHANGE_EVENT, isAuthenticated, setAuthenticated } from "../../app/authSession.js";
import { useSettings } from "../../app/settings.jsx";
import Avatar from "../common/Avatar.jsx";
import Button from "../common/Button.jsx";
import "./layout.css";

const links = [
  { to: "/home", labelKey: "nav.home" },
  { to: "/explore", labelKey: "nav.explore" },
  { to: "/community", labelKey: "nav.community" },
  { to: "/mentors", labelKey: "nav.mentors" },
  { to: "/saved", labelKey: "nav.saved" },
];

export default function Navbar() {
  const { language, setLanguage, theme, setTheme, t } = useSettings();
  const navigate = useNavigate();
  const [authenticated, setAuthenticatedState] = useState(() => isAuthenticated());
  const [user, setUser] = useState(null);

  useEffect(() => {
    const handleAuthChange = () => {
      setAuthenticatedState(isAuthenticated());
    };

    window.addEventListener(AUTH_CHANGE_EVENT, handleAuthChange);
    window.addEventListener("storage", handleAuthChange);
    return () => {
      window.removeEventListener(AUTH_CHANGE_EVENT, handleAuthChange);
      window.removeEventListener("storage", handleAuthChange);
    };
  }, []);

  useEffect(() => {
    let mounted = true;

    async function loadUser() {
      try {
        const me = await authApi.me();
        if (mounted) {
          setUser(me);
          setAuthenticated(true);
          setAuthenticatedState(true);
        }
      } catch {
        if (mounted) {
          setUser(null);
          setAuthenticated(false);
        }
      }
    }

    loadUser();
    return () => {
      mounted = false;
    };
  }, [authenticated]);

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch (error) {
      // Ignore backend errors and clear local session state anyway.
    } finally {
      setAuthenticated(false);
      navigate("/home");
    }
  };

  return (
    <header className="navbar">
      <NavLink to="/home" className="navbar__brand" aria-label="Campio home">
        <span>Campio</span>
      </NavLink>
      <nav className="navbar__links" aria-label="Primary navigation">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) =>
              `navbar__link${isActive ? " active" : ""}`
            }
          >
            <span>{t(link.labelKey)}</span>
          </NavLink>
        ))}
        {user?.role === "ADMIN" ? (
          <>
            <NavLink to="/admin/opportunities" className={({ isActive }) => `navbar__link${isActive ? " active" : ""}`}>
              <span>{t("nav.admin")}</span>
            </NavLink>
            <NavLink to="/admin/ingestion" className={({ isActive }) => `navbar__link${isActive ? " active" : ""}`}>
              <span>{t("nav.crawl")}</span>
            </NavLink>
            <NavLink to="/admin/mentors" className={({ isActive }) => `navbar__link${isActive ? " active" : ""}`}>
              <span>{t("nav.mentors")}</span>
            </NavLink>
          </>
        ) : null}
      </nav>
      <div className="navbar__actions">
        <button className="navbar__icon-button" type="button" aria-label={theme === "dark" ? t("settings.light") : t("settings.dark")} title={theme === "dark" ? t("settings.light") : t("settings.dark")} onClick={() => setTheme(theme === "dark" ? "light" : "dark")}>
          {theme === "dark" ? <Sun size={18} aria-hidden="true" /> : <Moon size={18} aria-hidden="true" />}
        </button>
        <select
          className="navbar__select"
          aria-label={t("settings.language")}
          value={language}
          onChange={(event) => setLanguage(event.target.value)}
        >
          <option value="ko">{t("settings.korean")}</option>
          <option value="en">{t("settings.english")}</option>
        </select>
        {authenticated ? (
          <>
            <Button variant="ghost" onClick={handleLogout}>
              {t("login.logout")}
            </Button>
            <NavLink to="/profile" className="navbar__profile" aria-label={t("nav.profile")}>
              <Avatar src={user?.avatarUrl} name={user?.name} size="sm" />
            </NavLink>
          </>
        ) : (
          <>
            <Button to="/login" variant="secondary">
              {t("login.action")}
            </Button>
            <Button to="/signup" variant="ghost">
              {t("login.create")}
            </Button>
          </>
        )}
      </div>
    </header>
  );
}
