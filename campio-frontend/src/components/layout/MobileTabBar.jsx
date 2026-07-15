import { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import { Bookmark, Compass, Home, LogIn, MessageCircle, User } from "lucide-react";
import { AUTH_CHANGE_EVENT, isAuthenticated } from "../../app/authSession.js";
import { useSettings } from "../../app/settings.jsx";
import "./layout.css";

const baseTabs = [
  { to: "/home", labelKey: "nav.home", icon: Home },
  { to: "/explore", labelKey: "nav.explore", icon: Compass },
  { to: "/saved", labelKey: "nav.saved", icon: Bookmark },
  { to: "/community", labelKey: "nav.community", icon: MessageCircle },
];

export default function MobileTabBar() {
  const { t } = useSettings();
  const [authenticated, setAuthenticatedState] = useState(() => isAuthenticated());

  useEffect(() => {
    const handleAuthChange = () => setAuthenticatedState(isAuthenticated());
    window.addEventListener(AUTH_CHANGE_EVENT, handleAuthChange);
    window.addEventListener("storage", handleAuthChange);
    return () => {
      window.removeEventListener(AUTH_CHANGE_EVENT, handleAuthChange);
      window.removeEventListener("storage", handleAuthChange);
    };
  }, []);

  const tabs = [
    ...baseTabs,
    authenticated
      ? { to: "/profile", labelKey: "nav.profile", icon: User }
      : { to: "/login", labelKey: "login.action", icon: LogIn },
  ];

  return (
    <nav className="mobile-tabs" aria-label="Mobile navigation">
      {tabs.map((tab) => {
        const Icon = tab.icon;
        return (
          <NavLink key={tab.to} to={tab.to} className="mobile-tabs__item">
            <Icon size={18} aria-hidden="true" />
            <span>{t(tab.labelKey)}</span>
          </NavLink>
        );
      })}
    </nav>
  );
}
