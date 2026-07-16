import { useEffect, useState } from "react";
import { Bookmark, Pencil, Trash2 } from "lucide-react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Avatar from "../components/common/Avatar.jsx";
import Badge from "../components/common/Badge.jsx";
import Button from "../components/common/Button.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import CommentList from "../components/community/CommentList.jsx";
import { communityApi } from "../api/communityApi.js";
import { isApiStatus } from "../api/client.js";
import { setAuthenticated } from "../app/authSession.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function CommunityDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { language, t } = useSettings();
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [comment, setComment] = useState("");
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({ title: "", content: "" });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;
    Promise.all([communityApi.postDetail(id), communityApi.listComments(id)])
      .then(([postData, commentData]) => {
        if (!mounted) return;
        setPost(postData);
        setComments(commentData);
        setEditForm({ title: postData.title, content: postData.content });
      })
      .catch((err) => mounted && setError(err.message || t("common.errorDescription")))
      .finally(() => mounted && setLoading(false));
    return () => { mounted = false; };
  }, [id, t]);

  function handleAuthError(err) {
    if (isApiStatus(err, 401)) {
      setAuthenticated(false);
      navigate("/login");
      return true;
    }
    setError(err.message || t("common.errorDescription"));
    return false;
  }

  if (loading) return <LoadingSkeleton count={3} />;
  if (!post) return <EmptyState title={t("common.errorTitle")} description={error} actionLabel={t("saved.explore")} actionTo="/community" />;

  return (
    <div className="page community-detail stack">
      <Link className="accent-link" to="/community">{language === "ko" ? "커뮤니티로 돌아가기" : "Back to community"}</Link>
      <article className="community-detail__post">
        <div className="community-detail__heading"><Badge>{post.type}</Badge>{post.relatedOpportunityTitle ? <Link to={`/opportunities/${post.opportunityId}`}>{post.relatedOpportunityTitle}</Link> : null}</div>
        {editing ? (
          <form className="form-grid" onSubmit={async (event) => {
            event.preventDefault();
            setSubmitting(true);
            try {
              const updated = await communityApi.updatePost(id, { ...post, title: editForm.title.trim(), content: editForm.content.trim() });
              setPost(updated); setEditing(false);
            } catch (err) { handleAuthError(err); } finally { setSubmitting(false); }
          }}>
            <input className="field__input" value={editForm.title} maxLength={200} required onChange={(event) => setEditForm((current) => ({ ...current, title: event.target.value }))} />
            <textarea className="field__input field__textarea" value={editForm.content} maxLength={10000} required onChange={(event) => setEditForm((current) => ({ ...current, content: event.target.value }))} />
            <div className="detail-actions"><Button type="submit" disabled={submitting}>{language === "ko" ? "수정 저장" : "Save changes"}</Button><Button variant="ghost" onClick={() => setEditing(false)}>{language === "ko" ? "취소" : "Cancel"}</Button></div>
          </form>
        ) : <><h1>{post.title}</h1><p className="community-detail__content">{post.content}</p></>}
        <div className="community-detail__footer">
          <div className="post-card__author"><Avatar src={post.authorAvatarUrl} name={post.authorName} size="sm" /><span>{post.authorName}</span><span>{post.createdAt}</span></div>
          <div className="inline-actions">
            <button type="button" className={post.saved ? "icon-button is-saved" : "icon-button"} aria-label="게시글 저장" title="게시글 저장" onClick={async () => {
              const saved = !post.saved; setPost((current) => ({ ...current, saved, savedCount: Math.max(0, current.savedCount + (saved ? 1 : -1)) }));
              try { if (saved) await communityApi.savePost(id); else await communityApi.unsavePost(id); } catch (err) { setPost((current) => ({ ...current, saved: !saved, savedCount: Math.max(0, current.savedCount + (saved ? -1 : 1)) })); handleAuthError(err); }
            }}><Bookmark size={17} fill={post.saved ? "currentColor" : "none"} aria-hidden="true" /><span>{post.savedCount}</span></button>
            {post.own ? <><button type="button" className="icon-button" aria-label="글 수정" title="글 수정" onClick={() => setEditing(true)}><Pencil size={17} aria-hidden="true" /></button><button type="button" className="icon-button" aria-label="글 삭제" title="글 삭제" onClick={async () => { if (!window.confirm(language === "ko" ? "이 글을 삭제할까요?" : "Delete this post?")) return; try { await communityApi.deletePost(id); navigate("/community"); } catch (err) { handleAuthError(err); } }}><Trash2 size={17} aria-hidden="true" /></button></> : null}
          </div>
        </div>
      </article>

      <section className="community-comments">
        <h2>{language === "ko" ? `댓글 ${comments.length}개` : `${comments.length} comments`}</h2>
        <form className="community-comment-form" onSubmit={async (event) => {
          event.preventDefault(); if (!comment.trim() || submitting) return; setSubmitting(true); setError("");
          try { const created = await communityApi.addComment(id, { content: comment.trim() }); setComments((current) => [...current, created]); setComment(""); setPost((current) => ({ ...current, commentCount: current.commentCount + 1 })); } catch (err) { handleAuthError(err); } finally { setSubmitting(false); }
        }}><textarea className="field__input field__textarea" value={comment} maxLength={3000} placeholder={language === "ko" ? "댓글을 입력하세요" : "Write a comment"} onChange={(event) => setComment(event.target.value)} /><Button type="submit" disabled={submitting || !comment.trim()}>{language === "ko" ? "댓글 등록" : "Add comment"}</Button></form>
        <CommentList comments={comments} onDelete={async (commentId) => { try { await communityApi.deleteComment(id, commentId); setComments((current) => current.filter((item) => item.id !== commentId)); setPost((current) => ({ ...current, commentCount: Math.max(0, current.commentCount - 1) })); } catch (err) { handleAuthError(err); } }} />
        {error ? <p className="form-error" role="alert">{error}</p> : null}
      </section>
    </div>
  );
}
