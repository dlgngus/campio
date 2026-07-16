import { useEffect, useState } from "react";
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
import "./pages.css";

const PAGE_SIZE = 12;

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
  const initialPage = Math.max(1, Number.parseInt(searchParams.get("page") || "1", 10) || 1);
  const [page, setPage] = useState(initialPage);
  const [opportunities, setOpportunities] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [savingIds, setSavingIds] = useState([]);

  const [reloadKey, setReloadKey] = useState(0);

  function searchRequest() {
    return {
      page: page - 1,
      size: PAGE_SIZE,
      q: filters.query.trim(),
      target: filters.target.trim(),
      category: filters.category === "All" ? "" : filters.category,
      region: filters.region === "All" ? "" : filters.region,
      deadlineSoon: filters.deadlineOnly,
      online: filters.onlineOnly,
      saved: filters.savedOnly,
      sort: filters.sortBy,
    };
  }

  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(async () => {
      setLoading(true);
      setError("");
      try {
        const result = await opportunityApi.search(searchRequest(), { signal: controller.signal });
        setOpportunities(result.content);
        setTotalElements(result.totalElements);
        setTotalPages(result.totalPages);
      } catch (err) {
        if (controller.signal.aborted) return;
        if (isApiStatus(err, 401) && filters.savedOnly) {
          setAuthenticated(false);
          navigate("/login");
          return;
        }
        setError(err.message || t("common.errorDescription"));
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    }, 300);
    return () => {
      window.clearTimeout(timer);
      controller.abort();
    };
  }, [filters, page, reloadKey, navigate, t]);

  function syncUrl(updated, nextPage = 1) {
    const params = new URLSearchParams();
    if (updated.query) params.set("q", updated.query);
    if (updated.target) params.set("target", updated.target);
    if (updated.category !== "All") params.set("category", updated.category);
    if (updated.region !== "All") params.set("region", updated.region);
    if (updated.sortBy !== "deadline") params.set("sort", updated.sortBy);
    if (updated.deadlineOnly) params.set("deadline", "soon");
    if (updated.onlineOnly) params.set("online", "true");
    if (updated.savedOnly) params.set("saved", "true");
    if (nextPage > 1) params.set("page", String(nextPage));
    navigate({ pathname: "/explore", search: params.toString() ? `?${params.toString()}` : "" }, { replace: true });
  }

  function updateFilters(next) {
    setPage(1);
    setFilters((current) => {
      const updated = { ...current, ...next };
      syncUrl(updated, 1);
      return updated;
    });
  }

  function changePage(nextPage) {
    setPage(nextPage);
    syncUrl(filters, nextPage);
    window.scrollTo({ top: 0, behavior: "smooth" });
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
      if (filters.savedOnly) setReloadKey((currentKey) => currentKey + 1);
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
          <p className="page-kicker">{totalElements} {t("explore.matches")}</p>
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
            onAction={() => setReloadKey((current) => current + 1)}
          />
        ) : (
          <div><OpportunityGrid opportunities={opportunities} onToggleSave={toggleSave} />{totalPages > 1 ? <nav className="pagination" aria-label="Pagination"><button type="button" disabled={page === 1} onClick={() => changePage(page - 1)}>{"<"}</button><span>{page} / {totalPages}</span><button type="button" disabled={page >= totalPages} onClick={() => changePage(page + 1)}>{">"}</button></nav> : null}</div>
        )}
      </div>
    </div>
  );
}
