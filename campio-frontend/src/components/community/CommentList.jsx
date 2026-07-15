import "./community.css";

export default function CommentList({ comments = [] }) {
  return (
    <div className="comment-list">
      {comments.map((comment) => (
        <p key={comment.id}>{comment.content}</p>
      ))}
    </div>
  );
}
