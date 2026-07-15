import { useEffect, useState } from "react";
import SectionHeader from "../components/common/SectionHeader.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import MentorCard from "../components/mentor/MentorCard.jsx";
import { mentorApi } from "../api/mentorApi.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function MentorsPage() {
  const { t } = useSettings();
  const [mentors, setMentors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadMentors(shouldUpdate = () => true) {
    setLoading(true);
    setError("");
    try {
      const items = await mentorApi.list();
      if (shouldUpdate()) setMentors(items);
    } catch (err) {
      if (shouldUpdate()) setError(err.message || t("common.errorDescription"));
    } finally {
      if (shouldUpdate()) setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    loadMentors(() => mounted);
    return () => {
      mounted = false;
    };
  }, [t]);

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <p className="page-kicker">{t("mentors.kicker")}</p>
          <h1 className="page-title">{t("mentors.title")}</h1>
        </div>
      </header>
      <SectionHeader title={t("mentors.available")} />
      {loading ? (
        <LoadingSkeleton count={3} />
      ) : error ? (
        <EmptyState
          title={t("common.errorTitle")}
          description={error}
          actionLabel={t("common.retry")}
          onAction={() => loadMentors()}
        />
      ) : !mentors.length ? (
        <EmptyState title={t("mentors.emptyTitle")} description={t("mentors.emptyDescription")} />
      ) : (
        <div className="simple-grid">
          {mentors.map((mentor) => (
            <MentorCard key={mentor.id} mentor={mentor} />
          ))}
        </div>
      )}
    </div>
  );
}
