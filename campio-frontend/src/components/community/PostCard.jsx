import Badge from "../common/Badge.jsx";
import { useSettings } from "../../app/settings.jsx";
import "./community.css";

export default function PostCard({ post }) {
  const { language } = useSettings();

  return (
    <article className="post-card">
      <div>
        <Badge>{post.type}</Badge>
        <h3>{post.title}</h3>
        <p>{post.relatedOpportunityTitle}</p>
      </div>
      <div className="post-card__meta">
        <span>{language === "ko" ? `댓글 ${post.commentCount}개` : `${post.commentCount} comments`}</span>
        <span>{language === "ko" ? `${post.savedCount}명이 저장` : `${post.savedCount} saves`}</span>
        <span>{post.createdAt}</span>
      </div>
    </article>
  );
}
