import { useEffect, useMemo, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { Bookmark, CalendarClock, Search, Sparkles, TrendingUp } from "lucide-react";
import EmptyState from "../components/common/EmptyState.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import SectionHeader from "../components/common/SectionHeader.jsx";
import FeaturedOpportunityCard from "../components/opportunity/FeaturedOpportunityCard.jsx";
import OpportunityGrid from "../components/opportunity/OpportunityGrid.jsx";
import { opportunityApi } from "../api/opportunityApi.js";
import { savedApi } from "../api/savedApi.js";
import { isApiStatus } from "../api/client.js";
import { setAuthenticated } from "../app/authSession.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function HomePage() {
  const { language, t } = useSettings();
  const navigate = useNavigate();
  const { user } = useOutletContext();
  const [data, setData] = useState({ recommended: [], closing: [], popular: [], all: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [savingIds, setSavingIds] = useState([]);
  const [query, setQuery] = useState("");

  async function loadHome(active = () => true) {
    setLoading(true);
    setError("");
    try {
      const feed = await opportunityApi.home();
      if (active()) {
        setData({
          recommended: feed.recommended,
          closing: feed.closing.slice(0, 4),
          popular: feed.popular.slice(0, 4),
          all: feed.latest,
        });
      }
    } catch (err) {
      if (active()) setError(err.message || t("common.errorDescription"));
    } finally {
      if (active()) setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    loadHome(() => mounted);
    return () => {
      mounted = false;
    };
  }, [t]);

  const newThisWeek = useMemo(() => {
    const threshold = Date.now() - 7 * 86400000;
    return data.all.filter((item) => item.newThisWeek || (item.createdAt && new Date(item.createdAt).getTime() >= threshold)).slice(0, 4);
  }, [data.all]);

  function updateSavedEverywhere(id, saved) {
    setData((current) => Object.fromEntries(
      Object.entries(current).map(([key, items]) => [key, items.map((item) => item.id === id ? { ...item, saved } : item)])
    ));
  }

  async function toggleSave(id) {
    if (savingIds.includes(id)) return;
    const opportunity = data.all.find((item) => item.id === id)
      || data.recommended.find((item) => item.id === id)
      || data.closing.find((item) => item.id === id)
      || data.popular.find((item) => item.id === id);
    if (!opportunity) return;
    const next = !opportunity.saved;
    setSavingIds((current) => [...current, id]);
    updateSavedEverywhere(id, next);
    try {
      if (next) await savedApi.save(id);
      else await savedApi.unsave(id);
    } catch (err) {
      updateSavedEverywhere(id, !next);
      if (isApiStatus(err, 401)) {
        setAuthenticated(false);
        navigate("/login");
      } else {
        setError(err.message || t("common.errorDescription"));
      }
    } finally {
      setSavingIds((current) => current.filter((savedId) => savedId !== id));
    }
  }

  if (loading) return <LoadingSkeleton count={5} />;
  if (error && !data.all.length) {
    return <EmptyState title={t("common.errorTitle")} description={error} actionLabel={t("common.retry")} onAction={() => loadHome()} />;
  }

  const greeting = user?.name
    ? language === "ko" ? `${user.name}님, 다음 기회를 확인하세요.` : `Welcome back, ${user.name}.`
    : language === "ko" ? "다음 기회를 발견하세요." : "Find your next opportunity.";
  const savedCount = data.all.filter((item) => item.saved).length;
  const featured = data.recommended[0];

  return (
    <div className="page home-dashboard stack">
      <header className="home-dashboard__header">
        <div><p className="page-kicker">{t("home.kicker")}</p><h1>{greeting}</h1></div>
        <form onSubmit={(event) => { event.preventDefault(); navigate(query.trim() ? `/explore?q=${encodeURIComponent(query.trim())}` : "/explore"); }} role="search" className="home-dashboard__search">
          <Search size={18} aria-hidden="true" />
          <input aria-label={t("filters.search")} value={query} onChange={(event) => setQuery(event.target.value)} placeholder={t("home.searchPlaceholder")} />
        </form>
      </header>

      <section className="home-metrics" aria-label={t("home.metricsTitle")}>
        <div><CalendarClock aria-hidden="true" /><span>{language === "ko" ? "14일 내 마감" : "Due in 14 days"}</span><strong>{data.closing.filter((item) => item.deadline && new Date(item.deadline) - new Date() <= 14 * 86400000).length}</strong></div>
        <div><Sparkles aria-hidden="true" /><span>{language === "ko" ? "맞춤 추천" : "Recommended"}</span><strong>{data.recommended.length}</strong></div>
        <div><TrendingUp aria-hidden="true" /><span>{language === "ko" ? "이번 주 신규" : "New this week"}</span><strong>{newThisWeek.length}</strong></div>
        <div><Bookmark aria-hidden="true" /><span>{language === "ko" ? "저장한 기회" : "Saved"}</span><strong>{savedCount}</strong></div>
      </section>

      {featured ? (
        <section><SectionHeader title={language === "ko" ? "추천 기회" : "Recommended for you"} actionLabel={t("saved.explore")} actionTo="/explore" /><FeaturedOpportunityCard opportunity={featured} reason={language === "ko" ? "관심사와 활동 분야를 반영했어요" : "Based on your interests"} ctaLabel={t("card.viewDetails")} onToggleSave={toggleSave} /></section>
      ) : <EmptyState title={language === "ko" ? "추천을 준비 중이에요." : "No recommendations yet."} description={language === "ko" ? "관심사를 추가하면 맞춤 기회를 보여드려요." : "Add interests to improve your feed."} actionLabel={t("common.goLogin")} actionTo={user ? "/profile" : "/login"} />}

      <section className="page-section"><SectionHeader title={language === "ko" ? "마감 임박" : "Closing soon"} actionLabel={t("saved.explore")} actionTo="/explore?deadline=soon" /><OpportunityGrid opportunities={data.closing} onToggleSave={toggleSave} variant="urgent" /></section>
      <section className="page-section"><SectionHeader title={language === "ko" ? "인기 기회" : "Popular opportunities"} actionLabel={t("saved.explore")} actionTo="/explore?sort=popular" /><OpportunityGrid opportunities={data.popular} onToggleSave={toggleSave} /></section>
      <section className="page-section"><SectionHeader title={language === "ko" ? "이번 주 새 기회" : "New this week"} /><OpportunityGrid opportunities={newThisWeek} onToggleSave={toggleSave} /></section>
      {error ? <p className="form-error" role="alert">{error}</p> : null}
    </div>
  );
}
