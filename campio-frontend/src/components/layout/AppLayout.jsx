import { Outlet } from "react-router-dom";
import Navbar from "./Navbar.jsx";
import MobileTabBar from "./MobileTabBar.jsx";

export default function AppLayout() {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="app-main">
        <Outlet />
      </main>
      <MobileTabBar />
    </div>
  );
}
