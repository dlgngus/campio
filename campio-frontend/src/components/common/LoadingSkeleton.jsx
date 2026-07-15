import "./common.css";

export default function LoadingSkeleton({ count = 3 }) {
  return (
    <div className="skeleton-list" aria-label="Loading content">
      {Array.from({ length: count }).map((_, index) => (
        <div className="skeleton-card" key={index}>
          <span />
          <strong />
          <p />
          <p />
        </div>
      ))}
    </div>
  );
}
