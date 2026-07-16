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

const tabs = ["Saved", "Deadline Soon", "APPLIED", "ARCHIVED"];

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
      const [items, records] = await Promise.all([
        savedApi.list(),
        applicationApi.list(),
      ]);
      const opportunities = await opportunityApi.batch(records.map((record) => record.opportunityId));
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
  const today = new Date();
  const deadlineSoon = saved.filter((item) => {
    if (!item.deadline) return false;
    const deadline = new Date(`${item.deadline}T00:00:00`);
    const days = Math.ceil((deadline - today) / (1000 * 60 * 60 * 24));
    return days >= 0 && days <= 14;
  });
  const recordsForTab = applications.filter((record) => record.status === tab);
  const recorded = allOpportunities
    .filter((item) => recordsForTab.some((record) => record.opportunityId === item.id))
    .map((item) => ({ ...item, saved: saved.some((savedItem) => savedItem.id === item.id) }));
  const visibleOpportunities = tab === "Deadline Soon" ? deadlineSoon : ["APPLIED", "ARCHIVED"].includes(tab) ? recorded : saved;

  async function toggleSave(id) {
    const existing = saved.find((item) => item.id === id);
    const opportunity = existing || allOpportunities.find((item) => item.id === id);
    if (!opportunity) return;
    const nextSaved = !existing;
    setSaved((current) => nextSaved ? [...current, { ...opportunity, saved: true }] : current.filter((item) => item.id !== id));
    try {
      if (nextSaved) await savedApi.save(id);
      else await savedApi.unsave(id);
    } catch (err) {
      setSaved((current) => nextSaved ? current.filter((item) => item.id !== id) : [...current, { ...opportunity, saved: true }]);
      if (isApiStatus(err, 401)) { setAuthenticated(false); navigate("/login"); }
      else setError(err.message || t("common.errorDescription"));
    }
  }

  async function updateApplication(record, status) {
    const previous = record.status;
    setApplications((current) => current.map((item) => item.id === record.id ? { ...item, status } : item));
    try { await applicationApi.update(record.id, { status, memo: record.memo || "" }); }
    catch (err) {
      setApplications((current) => current.map((item) => item.id === record.id ? { ...item, status: previous } : item));
      setError(err.message || t("common.errorDescription"));
    }
  }

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
            onToggleSave={toggleSave}
            emptyTitle={t("saved.emptyTitle")}
            emptyDescription={t("saved.emptyDescription")}
          />
        )}
        {!loading && !requiresLogin && ["APPLIED", "ARCHIVED"].includes(tab) && recordsForTab.length ? <div className="application-status-controls">{recordsForTab.map((record) => <label className="field" key={record.id}><span className="field__label">{allOpportunities.find((item) => item.id === record.opportunityId)?.title || `#${record.opportunityId}`}</span><select className="field__input" value={record.status} onChange={(event) => updateApplication(record, event.target.value)}>{["INTERESTED", "PREPARING", "APPLIED", "ACCEPTED", "REJECTED", "ARCHIVED"].map((status) => <option key={status} value={status}>{labelStatus(status)}</option>)}</select></label>)}</div> : null}
      </section>
    </div>
  );
}
