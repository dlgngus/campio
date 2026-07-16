import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import OpportunityFilters from "../components/opportunity/OpportunityFilters.jsx";
import { categories, regions, sortOptions } from "../components/opportunity/OpportunityFilters.jsx";
import OpportunityGrid from "../components/opportunity/OpportunityGrid.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import { useSettings } from "../app/settings.jsx";
import { setAuthenticated } from "../app/authSession.js";
import { isApiStatus } from "../api/client.js";
import { opportunityApi } from "../api/opportunityApi.js";
import { savedApi } from "../api/savedApi.js";
import { isStudentRelevantOpportunity } from "../app/studentOpportunityPolicy.js";
import { resolveOpportunityLocation } from "../app/opportunityLocation.js";
import "./pages.css";

export default function ExplorePage() {
  const { t } = useSettings();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialCategory = searchParams.get("category");
  const initialRegion = searchParams.get("region");
  const initialSort = searchParams.get("sort");
  const [filters, setFilters] = useState({
    query: searchParams.get("q") || "",
    target: searchParams.get("target") || "",
    category: categories.includes(initialCategory) ? initialCategory : "All",
    region: regions.includes(initialRegion) ? initialRegion : "All",
    sortBy: sortOptions.includes(initialSort) ? initialSort : "deadline",
    deadlineOnly: searchParams.get("deadline") === "soon",
    onlineOnly: searchParams.get("online") === "true",
    savedOnly: searchParams.get("saved") === "true",
  });
  const [page, setPage] = useState(1);
  const [opportunities, setOpportunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [savingIds, setSavingIds] = useState([]);

  const filtered = useMemo(() => {
    const query = filters.query.trim().toLowerCase();
    const target = filters.target.trim().toLowerCase();
    const today = new Date();
    const filteredItems = opportunities.filter((item) => {
      const location = resolveOpportunityLocation(item);
      const matchesQuery =
        !query ||
        [item.title, item.organization, item.category, location, ...item.tags]
          .join(" ")
          .toLowerCase()
          .includes(query);
      const matchesCategory = filters.category === "All" || item.category === filters.category;
      const matchesTarget = !target || String(item.target || "").toLowerCase().includes(target);
      const matchesRegion = filters.region === "All" || location === filters.region;
      const deadline = item.deadline ? new Date(`${item.deadline}T00:00:00`) : null;
      const daysToDeadline = deadline ? Math.ceil((deadline - today) / (1000 * 60 * 60 * 24)) : null;
      const matchesDeadline = !filters.deadlineOnly || (daysToDeadline !== null && daysToDeadline >= 0 && daysToDeadline <= 14);
      const matchesOnline = !filters.onlineOnly || item.isOnline;
      const matchesSaved = !filters.savedOnly || item.saved;
      return matchesQuery && matchesTarget && matchesCategory && matchesRegion && matchesDeadline && matchesOnline && matchesSaved;
    });
    return filteredItems.sort((a, b) => {
      if (filters.sortBy === "latest") {
        return String(b.createdAt || "").localeCompare(String(a.createdAt || ""));
      }
      if (filters.sortBy === "title") {
        return String(a.title || "").localeCompare(String(b.title || ""), "ko");
      }
      if (filters.sortBy === "popular") {
        return Number(b.popularityCount || 0) - Number(a.popularityCount || 0);
      }
      return String(a.deadline || "9999-12-31").localeCompare(String(b.deadline || "9999-12-31"));
    });
  }, [filters, opportunities]);
  const totalPages = Math.max(1, Math.ceil(filtered.length / 12));
  const paged = filtered.slice((page - 1) * 12, page * 12);

  async function loadOpportunities() {
    setLoading(true);
    setError("");
    try {
      const items = await opportunityApi.list();
      setOpportunities(items.filter(isStudentRelevantOpportunity));
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
        if (mounted) setOpportunities(items.filter(isStudentRelevantOpportunity));
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
    setPage(1);
    setFilters((current) => {
      const updated = { ...current, ...next };
      const params = new URLSearchParams();
      if (updated.query) params.set("q", updated.query);
      if (updated.target) params.set("target", updated.target);
      if (updated.category !== "All") params.set("category", updated.category);
      if (updated.region !== "All") params.set("region", updated.region);
      if (updated.sortBy !== "deadline") params.set("sort", updated.sortBy);
      if (updated.deadlineOnly) params.set("deadline", "soon");
      if (updated.onlineOnly) params.set("online", "true");
      if (updated.savedOnly) params.set("saved", "true");
      navigate({ pathname: "/explore", search: params.toString() ? `?${params.toString()}` : "" }, { replace: true });
      return updated;
    });
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
          <div><OpportunityGrid opportunities={paged} onToggleSave={toggleSave} />{totalPages > 1 ? <nav className="pagination" aria-label="Pagination"><button type="button" disabled={page === 1} onClick={() => setPage((current) => current - 1)}>{"<"}</button><span>{page} / {totalPages}</span><button type="button" disabled={page === totalPages} onClick={() => setPage((current) => current + 1)}>{">"}</button></nav> : null}</div>
        )}
      </div>
    </div>
  );
}
