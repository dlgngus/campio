import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Bookmark, ExternalLink } from "lucide-react";
import Badge from "../components/common/Badge.jsx";
import Button from "../components/common/Button.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import FilterChip from "../components/common/FilterChip.jsx";
import SectionHeader from "../components/common/SectionHeader.jsx";
import OpportunityGrid from "../components/opportunity/OpportunityGrid.jsx";
import DeadlineBadge from "../components/opportunity/DeadlineBadge.jsx";
import PostCard from "../components/community/PostCard.jsx";
import MentorCard from "../components/mentor/MentorCard.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import { communityApi } from "../api/communityApi.js";
import { mentorApi } from "../api/mentorApi.js";
import { opportunityApi } from "../api/opportunityApi.js";
import { applicationApi } from "../api/applicationApi.js";
import { savedApi } from "../api/savedApi.js";
import { isApiStatus } from "../api/client.js";
import { setAuthenticated } from "../app/authSession.js";
import { useSettings } from "../app/settings.jsx";
import { resolveOpportunityLocation } from "../app/opportunityLocation.js";
import "./pages.css";

const statuses = ["Interested", "Preparing", "Applied", "Accepted", "Rejected"];

export default function OpportunityDetailPage() {
  const { labelCategory, labelLocation, labelStatus, t } = useSettings();
  const { id } = useParams();
  const navigate = useNavigate();
  const [opportunity, setOpportunity] = useState(null);
  const [saved, setSaved] = useState(false);
  const [status, setStatus] = useState("Interested");
  const [applicationRecordId, setApplicationRecordId] = useState(null);
  const [recordSaving, setRecordSaving] = useState(false);
  const [related, setRelated] = useState([]);
  const [relatedPosts, setRelatedPosts] = useState([]);
  const [mentors, setMentors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  async function loadDetail(shouldUpdate = () => true) {
    setLoading(true);
    setError("");
    try {
      const detail = await opportunityApi.detail(id);
      if (!shouldUpdate()) return;
      setOpportunity(detail);
      setSaved(detail.saved);
      setLoading(false);

      const [relatedPage, posts, mentorList, records] = await Promise.all([
        opportunityApi.search({ page: 0, size: 4, category: detail.category, sort: "deadline" })
          .catch(() => ({ content: [] })),
        communityApi.listPosts().catch(() => []),
        mentorApi.list().catch(() => []),
        applicationApi.list().catch(() => []),
      ]);
      if (!shouldUpdate()) return;
      const record = records.find((item) => String(item.opportunityId) === String(detail.id));
      if (record) {
        setApplicationRecordId(record.id);
        setStatus(record.status || "Interested");
      } else {
        setApplicationRecordId(null);
        setStatus("Interested");
      }
      setRelated(
        relatedPage.content
          .filter((item) => item.id !== detail.id && item.category === detail.category)
          .slice(0, 3)
      );
      setRelatedPosts(posts.filter((post) => post.opportunityId === detail.id).slice(0, 2));
      setMentors(mentorList.slice(0, 3));
    } catch (err) {
      if (shouldUpdate()) {
        setOpportunity(null);
        setError(err.message || t("common.errorDescription"));
      }
    } finally {
      if (shouldUpdate()) setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    loadDetail(() => mounted);
    return () => {
      mounted = false;
    };
  }, [id, t]);

  if (loading) {
    return <LoadingSkeleton count={4} />;
  }

  if (!opportunity) {
    return (
      <EmptyState
        title={error ? t("common.errorTitle") : t("detail.notFoundTitle")}
        description={error || t("detail.notFoundDescription")}
        actionLabel={error ? t("common.retry") : undefined}
        onAction={error ? () => loadDetail() : undefined}
      />
    );
  }

  const displayLocation = resolveOpportunityLocation(opportunity);
  const fallback = t("detail.sourceFallback");
  const pendingInfo = t("detail.pendingInfo");
  const displayPeriod = [opportunity.startDate, opportunity.endDate].filter(Boolean).join(" - ") || pendingInfo;
  const isPlaceholder = (value) => !value
    || value.includes("원문 공고")
    || value.includes("원본 출처")
    || value.includes("official source");
  const description = isPlaceholder(opportunity.description) ? null : opportunity.description;

  function openApplyUrl() {
    try {
      const url = new URL(opportunity.applyUrl);
      if (!["http:", "https:"].includes(url.protocol)) throw new Error("Unsupported protocol");
      window.open(url.toString(), "_blank", "noopener,noreferrer");
    } catch {
      setError(t("detail.sourceFallback"));
    }
  }

  async function handleStatusChange(nextStatus) {
    if (recordSaving || nextStatus === status) return;
    const previousStatus = status;
    const previousId = applicationRecordId;
    setStatus(nextStatus);
    setRecordSaving(true);
    setError("");
    try {
      const body = { status: nextStatus, memo: "" };
      const record = previousId
        ? await applicationApi.update(previousId, body)
        : await applicationApi.saveForOpportunity(opportunity.id, body);
      setApplicationRecordId(record.id);
    } catch (err) {
      setStatus(previousStatus);
      setApplicationRecordId(previousId);
      if (isApiStatus(err, 401)) {
        setAuthenticated(false);
        navigate("/login");
        return;
      }
      setError(err.message || t("common.errorDescription"));
    } finally {
      setRecordSaving(false);
    }
  }

  return (
    <div className="page stack">
      <section className="detail-hero">
        <div>
          <Badge>{labelCategory(opportunity.category)}</Badge>
          <h1>{opportunity.title}</h1>
          <p>{opportunity.organization}</p>
          <div className="featured-opportunity__meta">
            <DeadlineBadge deadline={opportunity.deadline} />
            <span>{labelLocation(displayLocation)}</span>
            <span>{opportunity.isOnline ? t("filters.online") : t("filters.offline")}</span>
          </div>
        </div>
        <div className="detail-actions">
          <Button icon={ExternalLink} onClick={openApplyUrl} disabled={!opportunity.applyUrl}>
            {t("detail.apply")}
          </Button>
          <Button
            variant={saved ? "primary" : "secondary"}
            icon={Bookmark}
            aria-pressed={saved}
            disabled={saving}
            onClick={async () => {
              if (saving) return;
              const next = !saved;
              setSaved(next);
              setSaving(true);
              setError("");
              try {
                if (next) {
                  await savedApi.save(opportunity.id);
                } else {
                  await savedApi.unsave(opportunity.id);
                }
              } catch (err) {
                setSaved(!next);
                if (isApiStatus(err, 401)) {
                  setAuthenticated(false);
                  navigate("/login");
                  return;
                }
                setError(err.message || t("common.errorDescription"));
              } finally {
                setSaving(false);
              }
            }}
          >
            {saved ? t("card.saved") : t("card.save")}
          </Button>
          {error ? <p className="form-error">{error}</p> : null}
        </div>
      </section>

      <section className="info-grid">
        <div className="info-item">
          <span>{t("detail.deadline")}</span>
          <strong>{opportunity.deadline || fallback}</strong>
        </div>
        <div className="info-item">
          <span>{t("detail.location")}</span>
          <strong>{labelLocation(displayLocation)}</strong>
        </div>
        <div className="info-item">
          <span>{t("detail.period")}</span>
          <strong>{displayPeriod}</strong>
        </div>
        <div className="info-item">
          <span>{t("detail.target")}</span>
          <strong>{opportunity.target || pendingInfo}</strong>
        </div>
      </section>

      <section className="two-column page-section">
        <div className="detail-copy">
          <div className="copy-panel">
            <h2>{t("detail.description")}</h2>
            <p className="pre-wrap">{description || pendingInfo}</p>
          </div>
          {opportunity.requirements ? <div className="copy-panel">
            <h2>{t("detail.requirements")}</h2>
            <p className="pre-wrap">{opportunity.requirements}</p>
          </div> : null}
          {opportunity.benefits ? <div className="copy-panel">
            <h2>{t("detail.benefits")}</h2>
            <p className="pre-wrap">{opportunity.benefits}</p>
          </div> : null}
          {opportunity.applicationMethod ? <div className="copy-panel">
            <h2>{t("detail.howToApply")}</h2>
            <p className="pre-wrap">{opportunity.applicationMethod}</p>
          </div> : null}
        </div>
        <aside className="copy-panel">
          <h2>{t("detail.record")}</h2>
          <div className="status-list">
            {statuses.map((item) => (
              <FilterChip key={item} selected={status === item} onClick={() => handleStatusChange(item)} disabled={recordSaving}>
                {labelStatus(item)}
              </FilterChip>
            ))}
          </div>
        </aside>
      </section>

      <section className="page-section">
        <SectionHeader title={t("detail.related")} />
        <OpportunityGrid opportunities={related} />
      </section>

      <section className="page-section">
        <SectionHeader title={t("detail.qa")} actionLabel={t("detail.openCommunity")} actionTo="/community" />
        <div className="simple-grid">
          {(relatedPosts.length ? relatedPosts : []).map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      </section>

      <section className="page-section">
        <SectionHeader title={t("detail.mentors")} actionLabel={t("detail.viewMentors")} actionTo="/mentors" />
        <div className="simple-grid">
          {mentors.map((mentor) => (
            <MentorCard key={mentor.id} mentor={mentor} />
          ))}
        </div>
      </section>
    </div>
  );
}
