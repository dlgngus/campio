import { Bookmark, MapPin, Users } from "lucide-react";
import { Link } from "react-router-dom";
import Badge from "../common/Badge.jsx";
import Button from "../common/Button.jsx";
import { useSettings } from "../../app/settings.jsx";
import { resolveOpportunityLocation, visibleOpportunityTags } from "../../app/opportunityLocation.js";
import DeadlineBadge from "./DeadlineBadge.jsx";
import "./opportunity.css";

export default function OpportunityCard({
  opportunity,
  onToggleSave,
  variant = "default",
  density = "default",
  layout = "default",
}) {
  const { language, labelCategory, labelLocation, t } = useSettings();
  const isUrgent = variant === "urgent";
  const isHomeLayout = layout === "home";
  const displayLocation = resolveOpportunityLocation(opportunity);
  const displayTags = visibleOpportunityTags(opportunity);

  return (
    <article
      className={[
        "opportunity-card",
        density === "compact" ? "opportunity-card--compact" : "",
        isHomeLayout ? "opportunity-card--home" : "",
        opportunity.saved ? "is-saved" : "",
        isUrgent ? "opportunity-card--urgent" : "",
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {isHomeLayout ? (
        <>
          <div className="opportunity-card__top opportunity-card__top--home">
            <div className="opportunity-card__home-company">
              <p>{opportunity.organization}</p>
              <Badge>{labelCategory(opportunity.category)}</Badge>
            </div>
            <DeadlineBadge deadline={opportunity.deadline} urgent={isUrgent} />
          </div>
          <Link to={`/opportunities/${opportunity.id}`} className="opportunity-card__title">
            {opportunity.title}
          </Link>
          <div className="opportunity-card__actions">
            <Button to={`/opportunities/${opportunity.id}`}>
              {t("detail.apply")}
            </Button>
            <Button
              variant={opportunity.saved ? "primary" : "secondary"}
              icon={Bookmark}
              aria-pressed={opportunity.saved}
              onClick={() => onToggleSave?.(opportunity.id)}
            >
              {opportunity.saved ? t("card.saved") : t("card.save")}
            </Button>
          </div>
        </>
      ) : (
        <>
          <div className="opportunity-card__top">
            <Badge>{labelCategory(opportunity.category)}</Badge>
            <DeadlineBadge deadline={opportunity.deadline} urgent={isUrgent} />
          </div>
          <Link to={`/opportunities/${opportunity.id}`} className="opportunity-card__title">
            {opportunity.title}
          </Link>
          <p className="opportunity-card__org">{opportunity.organization}</p>
          <div className="opportunity-card__meta">
            <span>
              <MapPin size={15} aria-hidden="true" />
              {opportunity.isOnline ? t("filters.online") : labelLocation(displayLocation)}
            </span>
            <span>
              <Users size={15} aria-hidden="true" />
              {language === "ko"
                ? `${opportunity.popularityCount}${t("card.saves")}`
                : `${opportunity.popularityCount} ${t("card.saves")}`}
            </span>
          </div>
          <div className="opportunity-card__tags">
            {displayTags.map((tag) => (
              <span key={tag}>{tag}</span>
            ))}
          </div>
          <div className="opportunity-card__actions">
            <Button to={`/opportunities/${opportunity.id}`} variant="secondary">
              {t("card.viewDetails")}
            </Button>
            <Button
              variant={opportunity.saved ? "primary" : "secondary"}
              icon={Bookmark}
              aria-pressed={opportunity.saved}
              onClick={() => onToggleSave?.(opportunity.id)}
            >
              {opportunity.saved ? t("card.saved") : t("card.save")}
            </Button>
          </div>
        </>
      )}
    </article>
  );
}
