import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Avatar from "../components/common/Avatar.jsx";
import Badge from "../components/common/Badge.jsx";
import SectionHeader from "../components/common/SectionHeader.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import { authApi } from "../api/authApi.js";
import { savedApi } from "../api/savedApi.js";
import { isApiStatus } from "../api/client.js";
import { setAuthenticated } from "../app/authSession.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function ProfilePage() {
  const { labelCategory, t } = useSettings();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [savedCount, setSavedCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [requiresLogin, setRequiresLogin] = useState(false);

  async function loadProfile(shouldUpdate = () => true) {
    setLoading(true);
    setError("");
    setRequiresLogin(false);
    try {
      const [me, saved] = await Promise.all([authApi.me(), savedApi.list()]);
      if (shouldUpdate()) {
        setUser(me);
        setSavedCount(saved.length);
      }
    } catch (err) {
      if (!shouldUpdate()) return;
      if (isApiStatus(err, 401)) {
        setAuthenticated(false);
        setRequiresLogin(true);
      } else {
        setError(err.message || t("common.errorDescription"));
      }
    } finally {
      if (shouldUpdate()) setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    loadProfile(() => mounted);
    return () => {
      mounted = false;
    };
  }, [t]);

  if (loading) {
    return <LoadingSkeleton count={3} />;
  }

  if (requiresLogin) {
    return (
      <EmptyState
        title={t("common.loginRequiredTitle")}
        description={t("common.loginRequiredDescription")}
        actionLabel={t("common.goLogin")}
        onAction={() => navigate("/login")}
      />
    );
  }

  if (error || !user) {
    return (
      <EmptyState
        title={t("common.errorTitle")}
        description={error || t("common.errorDescription")}
        actionLabel={t("common.retry")}
        onAction={() => loadProfile()}
      />
    );
  }

  return (
    <div className="page">
      <header className="page-header">
        <div className="profile-heading">
          <Avatar src={user.avatarUrl} name={user.name} size="lg" />
          <div>
            <p className="page-kicker">{t("profile.kicker")}</p>
            <h1 className="page-title">{user.name}.</h1>
          </div>
        </div>
      </header>
      <div className="simple-grid">
        <div className="copy-panel">
          <h2>{t("profile.school")}</h2>
          <p>{user.school || "-"}</p>
        </div>
        <div className="copy-panel">
          <h2>{t("profile.major")}</h2>
          <p>{user.major || "-"} · {t("onboarding.grade")} {user.grade || "-"}</p>
        </div>
        <div className="copy-panel">
          <h2>{t("profile.saved")}</h2>
          <p>{savedCount} {t("home.saved")}</p>
        </div>
      </div>
      <section className="page-section">
        <SectionHeader title={t("profile.interests")} />
        <div className="status-list">
          {(user.interests ? user.interests.split(",").filter(Boolean) : ["Internship", "Scholarship", "Research"]).map((item) => (
            <Badge key={item}>{labelCategory(item)}</Badge>
          ))}
        </div>
      </section>
    </div>
  );
}
