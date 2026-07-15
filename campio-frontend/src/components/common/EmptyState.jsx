import { SearchX } from "lucide-react";
import Button from "./Button.jsx";
import "./common.css";

export default function EmptyState({ title, description, actionLabel, actionTo, onAction }) {
  return (
    <div className="empty-state">
      <SearchX size={22} aria-hidden="true" />
      <h3>{title}</h3>
      <p>{description}</p>
      {actionLabel ? (
        <Button variant="secondary" to={actionTo} onClick={onAction}>
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}
