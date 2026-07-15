import Button from "./Button.jsx";
import "./common.css";

export default function SectionHeader({ eyebrow, title, actionLabel, actionTo, id }) {
  return (
    <div className="section-header">
      <div>
        {eyebrow ? <p className="section-header__eyebrow">{eyebrow}</p> : null}
        <h2 id={id}>{title}</h2>
      </div>
      {actionLabel && actionTo ? (
        <Button to={actionTo} variant="secondary">
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}
