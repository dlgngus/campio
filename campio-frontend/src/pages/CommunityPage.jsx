import { useEffect, useState } from "react";
import SectionHeader from "../components/common/SectionHeader.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import PostCard from "../components/community/PostCard.jsx";
import { communityApi } from "../api/communityApi.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function CommunityPage() {
  const { t } = useSettings();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadPosts(shouldUpdate = () => true) {
    setLoading(true);
    setError("");
    try {
      const items = await communityApi.listPosts();
      if (shouldUpdate()) setPosts(items);
    } catch (err) {
      if (shouldUpdate()) setError(err.message || t("common.errorDescription"));
    } finally {
      if (shouldUpdate()) setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    loadPosts(() => mounted);
    return () => {
      mounted = false;
    };
  }, [t]);

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <p className="page-kicker">{t("community.kicker")}</p>
          <h1 className="page-title">{t("community.title")}</h1>
        </div>
      </header>
      <section>
        <SectionHeader title={t("community.popular")} />
        {loading ? (
          <LoadingSkeleton count={3} />
        ) : error ? (
          <EmptyState
            title={t("common.errorTitle")}
            description={error}
            actionLabel={t("common.retry")}
            onAction={() => loadPosts()}
          />
        ) : !posts.length ? (
          <EmptyState title={t("common.emptyTitle")} description={t("common.emptyDescription")} />
        ) : (
          <div className="simple-grid">
            {posts.map((post) => (
              <PostCard key={post.id} post={post} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
