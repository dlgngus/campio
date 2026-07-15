import { ArrowRight, Bookmark, Sparkles } from "lucide-react";
import { Link } from "react-router-dom";
import Badge from "../common/Badge.jsx";
import Button from "../common/Button.jsx";
import { useSettings } from "../../app/settings.jsx";
import { resolveOpportunityLocation, visibleOpportunityTags } from "../../app/opportunityLocation.js";
import DeadlineBadge from "./DeadlineBadge.jsx";
import "./opportunity.css";

export default function FeaturedOpportunityCard({
  opportunity,
  reason,
  ctaLabel = "View Details",
  matchPercent = 92,
  onToggleSave,
  compact = false,
}) {
  const { labelCategory, labelLocation, t } = useSettings();
  const displayLocation = resolveOpportunityLocation(opportunity);
  const displayTags = visibleOpportunityTags(opportunity);

  return (
    <article
      className={[
        "featured-opportunity",
        "featured-opportunity--home",
        compact ? "featured-opportunity--compact" : "",
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <div className="featured-opportunity__content">
        <div className="featured-opportunity__eyebrow">
          <span>{t("home.featuredSection")}</span>
          <p className="featured-opportunity__reason">
            <Sparkles size={15} aria-hidden="true" />
            {reason}
          </p>
        </div>
        <div className="featured-opportunity__layout">
          <div className="featured-opportunity__main">
            <p className="featured-opportunity__company">{opportunity.organization}</p>
            <h2>
              <Link to={`/opportunities/${opportunity.id}`}>{opportunity.title}</Link>
            </h2>
            <p className="featured-opportunity__copy">{opportunity.description}</p>
            <div className="featured-opportunity__chips">
              <Badge>{labelCategory(opportunity.category)}</Badge>
              {displayTags.map((tag) => (
                <Badge key={tag}>{tag}</Badge>
              ))}
            </div>
          </div>
          <div className="featured-opportunity__rail">
            <div className="featured-opportunity__meta">
              <div>
                <span>{t("detail.deadline")}</span>
                <DeadlineBadge deadline={opportunity.deadline} accent />
              </div>
              <div>
                <span>{t("detail.location")}</span>
                <strong>{labelLocation(displayLocation)}</strong>
              </div>
            </div>
            <div className="featured-opportunity__actions">
              <Button to={`/opportunities/${opportunity.id}`} icon={ArrowRight}>
                {ctaLabel}
              </Button>
              <Button
                variant="secondary"
                icon={Bookmark}
                aria-pressed={opportunity.saved}
                onClick={() => onToggleSave?.(opportunity.id)}
              >
                {opportunity.saved ? t("card.saved") : t("card.save")}
              </Button>
            </div>
          </div>
        </div>
        <div className="featured-opportunity__footer">
          <p className="featured-opportunity__match">
            {matchPercent}% {t("home.matchLabel")}
          </p>
          <p className="featured-opportunity__footer-copy">{t("home.featuredNote")}</p>
        </div>
      </div>
    </article>
  );
}
