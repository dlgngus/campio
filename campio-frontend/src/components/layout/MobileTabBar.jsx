import { NavLink } from "react-router-dom";
import { Bookmark, Compass, Home, MessageCircle } from "lucide-react";
import { useSettings } from "../../app/settings.jsx";
import "./layout.css";

const tabs = [
  { to: "/home", labelKey: "nav.home", icon: Home },
  { to: "/explore", labelKey: "nav.explore", icon: Compass },
  { to: "/saved", labelKey: "nav.saved", icon: Bookmark },
  { to: "/community", labelKey: "nav.community", icon: MessageCircle },
];

export default function MobileTabBar() {
  const { t } = useSettings();

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
