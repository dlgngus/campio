import { NavLink } from "react-router-dom";
import { Bookmark, Compass, Home, LogIn, MessageCircle, User } from "lucide-react";
import { useSettings } from "../../app/settings.jsx";
import "./layout.css";

const baseTabs = [
  { to: "/home", labelKey: "nav.home", icon: Home },
  { to: "/explore", labelKey: "nav.explore", icon: Compass },
  { to: "/saved", labelKey: "nav.saved", icon: Bookmark },
  { to: "/community", labelKey: "nav.community", icon: MessageCircle },
];

export default function MobileTabBar({ authenticated }) {
  const { t } = useSettings();
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
