import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import FilterChip from "../components/common/FilterChip.jsx";
import SectionHeader from "../components/common/SectionHeader.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import OpportunityGrid from "../components/opportunity/OpportunityGrid.jsx";
import { applicationApi } from "../api/applicationApi.js";
import { opportunityApi } from "../api/opportunityApi.js";
import { savedApi } from "../api/savedApi.js";
import { isApiStatus } from "../api/client.js";
import { setAuthenticated } from "../app/authSession.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

const tabs = ["Saved", "Deadline Soon", "Applied"];

export default function SavedPage() {
  const { labelStatus, t } = useSettings();
  const navigate = useNavigate();
  const [tab, setTab] = useState("Saved");
  const [saved, setSaved] = useState([]);
  const [applications, setApplications] = useState([]);
  const [allOpportunities, setAllOpportunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [requiresLogin, setRequiresLogin] = useState(false);

  async function loadSaved(shouldUpdate = () => true) {
    setLoading(true);
    setError("");
    setRequiresLogin(false);
    try {
      const [items, records, opportunities] = await Promise.all([
        savedApi.list(),
        applicationApi.list(),
        opportunityApi.list(),
      ]);
      if (shouldUpdate()) {
        setSaved(items);
        setApplications(records);
        setAllOpportunities(opportunities);
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
    loadSaved(() => mounted);
    return () => {
      mounted = false;
    };
  }, [t]);

  const applicationByOpportunityId = new Map(applications.map((record) => [record.opportunityId, record]));
  const appliedOpportunityIds = new Set(
    applications
      .filter((record) => record.status === "Applied")
      .map((record) => record.opportunityId)
  );
  const today = new Date();
  const deadlineSoon = saved.filter((item) => {
    if (!item.deadline) return false;
    const deadline = new Date(`${item.deadline}T00:00:00`);
    const days = Math.ceil((deadline - today) / (1000 * 60 * 60 * 24));
    return days >= 0 && days <= 14;
  });
  const applied = allOpportunities
    .filter((item) => appliedOpportunityIds.has(item.id))
    .map((item) => ({ ...item, saved: saved.some((savedItem) => savedItem.id === item.id) || Boolean(applicationByOpportunityId.get(item.id)) }));
  const visibleOpportunities = tab === "Deadline Soon" ? deadlineSoon : tab === "Applied" ? applied : saved;

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <p className="page-kicker">{t("saved.kicker")}</p>
          <h1 className="page-title">{t("saved.title")}</h1>
        </div>
      </header>
      <div className="status-list">
        {tabs.map((item) => (
          <FilterChip key={item} selected={tab === item} onClick={() => setTab(item)}>
            {labelStatus(item)}
          </FilterChip>
        ))}
      </div>
      <section className="page-section">
        <SectionHeader title={labelStatus(tab)} actionLabel={t("saved.explore")} actionTo="/explore" />
        {loading ? (
          <LoadingSkeleton count={4} />
        ) : requiresLogin ? (
          <EmptyState
            title={t("common.loginRequiredTitle")}
            description={t("common.loginRequiredDescription")}
            actionLabel={t("common.goLogin")}
            onAction={() => navigate("/login")}
          />
        ) : error ? (
          <EmptyState
            title={t("common.errorTitle")}
            description={error}
            actionLabel={t("common.retry")}
            onAction={() => loadSaved()}
          />
        ) : (
          <OpportunityGrid
            opportunities={visibleOpportunities}
            emptyTitle={t("saved.emptyTitle")}
            emptyDescription={t("saved.emptyDescription")}
          />
        )}
      </section>
    </div>
  );
}
