import { Bookmark, MessageCircle } from "lucide-react";
import { Link } from "react-router-dom";
import Avatar from "../common/Avatar.jsx";
import Badge from "../common/Badge.jsx";
import { useSettings } from "../../app/settings.jsx";
import "./community.css";

export default function PostCard({ post, onToggleSave }) {
  const { language } = useSettings();

  return (
    <article className="post-card">
      <div>
        <Badge>{post.type}</Badge>
        <h3><Link to={`/community/${post.id}`}>{post.title}</Link></h3>
        {post.relatedOpportunityTitle ? <p>{post.relatedOpportunityTitle}</p> : null}
      </div>
      <div className="post-card__author">
        <Avatar src={post.authorAvatarUrl} name={post.authorName} size="sm" />
        <span>{post.authorName}</span>
      </div>
      <div className="post-card__meta">
        <span><MessageCircle size={15} aria-hidden="true" />{post.commentCount}</span>
        <button type="button" className={post.saved ? "post-save is-saved" : "post-save"} aria-pressed={post.saved} aria-label={language === "ko" ? "게시글 저장" : "Save post"} title={language === "ko" ? "게시글 저장" : "Save post"} onClick={() => onToggleSave?.(post.id)}><Bookmark size={15} fill={post.saved ? "currentColor" : "none"} aria-hidden="true" />{post.savedCount}</button>
        <span>{post.createdAt}</span>
      </div>
    </article>
  );
}
