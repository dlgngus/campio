import { Trash2 } from "lucide-react";
import Avatar from "../common/Avatar.jsx";
import "./community.css";

export default function CommentList({ comments = [], onDelete }) {
  return (
    <div className="comment-list">
      {comments.map((comment) => (
        <article key={comment.id} className="comment-item">
          <Avatar src={comment.authorAvatarUrl} name={comment.authorName} size="sm" />
          <div><div className="comment-item__meta"><strong>{comment.authorName}</strong><span>{comment.createdAt}</span></div><p>{comment.content}</p></div>
          {comment.own ? <button type="button" className="icon-button" aria-label="댓글 삭제" title="댓글 삭제" onClick={() => onDelete?.(comment.id)}><Trash2 size={16} aria-hidden="true" /></button> : null}
        </article>
      ))}
    </div>
  );
}
