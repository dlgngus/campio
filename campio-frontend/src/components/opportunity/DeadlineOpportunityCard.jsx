import { ArrowRight } from "lucide-react";
import { Link } from "react-router-dom";
import Button from "../common/Button.jsx";
import Badge from "../common/Badge.jsx";
import DeadlineBadge from "./DeadlineBadge.jsx";
import { useSettings } from "../../app/settings.jsx";
import "./opportunity.css";

export default function DeadlineOpportunityCard({ opportunity }) {
  const { t, labelCategory } = useSettings();

  return (
    <article className="deadline-opportunity-card">
      <div className="deadline-opportunity-card__top">
        <DeadlineBadge deadline={opportunity.deadline} accent />
        <Badge>{labelCategory(opportunity.category)}</Badge>
      </div>
      <p className="deadline-opportunity-card__org">{opportunity.organization}</p>
      <Link to={`/opportunities/${opportunity.id}`} className="deadline-opportunity-card__title">
        {opportunity.title}
      </Link>
      <div className="deadline-opportunity-card__meta">
        <span>{t("detail.deadline")}</span>
        <strong>{opportunity.deadline}</strong>
      </div>
      <div className="deadline-opportunity-card__actions">
        <Button to={`/opportunities/${opportunity.id}`} icon={ArrowRight}>
          {t("detail.apply")}
        </Button>
      </div>
    </article>
  );
}
