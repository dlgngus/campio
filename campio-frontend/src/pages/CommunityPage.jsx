import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button.jsx";
import Input from "../components/common/Input.jsx";
import SectionHeader from "../components/common/SectionHeader.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import PostCard from "../components/community/PostCard.jsx";
import { communityApi } from "../api/communityApi.js";
import { isApiStatus } from "../api/client.js";
import { setAuthenticated } from "../app/authSession.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function CommunityPage() {
  const { t } = useSettings();
  const navigate = useNavigate();
  const [posts, setPosts] = useState([]);
  const [form, setForm] = useState({ title: "", content: "" });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
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
        <form
          className="community-composer"
          onSubmit={async (event) => {
            event.preventDefault();
            if (!form.title.trim() || !form.content.trim() || submitting) return;
            setSubmitting(true);
            setError("");
            try {
              const post = await communityApi.createPost({
                type: "QUESTION",
                title: form.title.trim(),
                content: form.content.trim(),
              });
              setPosts((current) => [post, ...current]);
              setForm({ title: "", content: "" });
            } catch (err) {
              if (isApiStatus(err, 401)) {
                setAuthenticated(false);
                navigate("/login");
                return;
              }
              setError(err.message || t("common.errorDescription"));
            } finally {
              setSubmitting(false);
            }
          }}
        >
          <Input
            label={t("community.writeTitle")}
            name="postTitle"
            placeholder={t("community.writeTitlePlaceholder")}
            value={form.title}
            onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))}
          />
          <label className="field">
            <span className="field__label">{t("community.writeContent")}</span>
            <textarea
              className="field__input field__textarea"
              placeholder={t("community.writeContentPlaceholder")}
              value={form.content}
              onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))}
            />
          </label>
          <Button type="submit" disabled={submitting}>
            {submitting ? t("common.loading") : t("community.writeAction")}
          </Button>
        </form>
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
          <EmptyState title={t("community.emptyTitle")} description={t("community.emptyDescription")} />
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
