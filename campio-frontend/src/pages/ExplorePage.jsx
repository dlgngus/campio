import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import OpportunityFilters from "../components/opportunity/OpportunityFilters.jsx";
import { categories } from "../components/opportunity/OpportunityFilters.jsx";
import OpportunityGrid from "../components/opportunity/OpportunityGrid.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import { useSettings } from "../app/settings.jsx";
import { setAuthenticated } from "../app/authSession.js";
import { isApiStatus } from "../api/client.js";
import { opportunityApi } from "../api/opportunityApi.js";
import { savedApi } from "../api/savedApi.js";
import "./pages.css";

export default function ExplorePage() {
  const { t } = useSettings();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialCategory = searchParams.get("category");
  const [filters, setFilters] = useState({
    query: searchParams.get("q") || "",
    category: categories.includes(initialCategory) ? initialCategory : "All",
    onlineOnly: false,
    savedOnly: false,
  });
  const [opportunities, setOpportunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [savingIds, setSavingIds] = useState([]);

  const filtered = useMemo(() => {
    const query = filters.query.trim().toLowerCase();
    return opportunities.filter((item) => {
      const matchesQuery =
        !query ||
        [item.title, item.organization, item.category, ...item.tags]
          .join(" ")
          .toLowerCase()
          .includes(query);
      const matchesCategory = filters.category === "All" || item.category === filters.category;
      const matchesOnline = !filters.onlineOnly || item.isOnline;
      const matchesSaved = !filters.savedOnly || item.saved;
      return matchesQuery && matchesCategory && matchesOnline && matchesSaved;
    });
  }, [filters, opportunities]);

  async function loadOpportunities() {
    setLoading(true);
    setError("");
    try {
      const items = await opportunityApi.list();
      setOpportunities(items);
    } catch (err) {
      setError(err.message || t("common.errorDescription"));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    async function load() {
      setLoading(true);
      setError("");
      try {
        const items = await opportunityApi.list();
        if (mounted) setOpportunities(items);
      } catch (err) {
        if (mounted) setError(err.message || t("common.errorDescription"));
      } finally {
        if (mounted) setLoading(false);
      }
    }
    load();
    return () => {
      mounted = false;
    };
  }, [t]);

  function updateFilters(next) {
    setFilters((current) => ({ ...current, ...next }));
  }

  async function toggleSave(id) {
    if (savingIds.includes(id)) {
      return;
    }
    const current = opportunities.find((item) => item.id === id);
    if (!current) {
      return;
    }

    setSavingIds((ids) => [...ids, id]);
    setError("");
    const nextSaved = !current.saved;
    setOpportunities((items) => items.map((item) => (item.id === id ? { ...item, saved: nextSaved } : item)));
    try {
      if (current.saved) {
        await savedApi.unsave(id);
      } else {
        await savedApi.save(id);
      }
    } catch (err) {
      setOpportunities((items) => items.map((item) => (item.id === id ? { ...item, saved: current.saved } : item)));
      if (isApiStatus(err, 401)) {
        setAuthenticated(false);
        navigate("/login");
        return;
      }
      setError(err.message || t("common.errorDescription"));
    } finally {
      setSavingIds((ids) => ids.filter((item) => item !== id));
    }
  }

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <p className="page-kicker">{filtered.length} {t("explore.matches")}</p>
          <h1 className="page-title">{t("explore.title")}</h1>
        </div>
      </header>
      <div className="explore-layout">
        <OpportunityFilters {...filters} onChange={updateFilters} />
        {loading ? (
          <LoadingSkeleton count={6} />
        ) : error ? (
          <EmptyState
            title={t("common.errorTitle")}
            description={error}
            actionLabel={t("common.retry")}
            onAction={loadOpportunities}
          />
        ) : (
          <OpportunityGrid opportunities={filtered} onToggleSave={toggleSave} />
        )}
      </div>
    </div>
  );
}
