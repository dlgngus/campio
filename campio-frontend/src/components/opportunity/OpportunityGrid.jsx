import EmptyState from "../common/EmptyState.jsx";
import { useSettings } from "../../app/settings.jsx";
import OpportunityCard from "./OpportunityCard.jsx";
import "./opportunity.css";

export default function OpportunityGrid({
  opportunities,
  onToggleSave,
  emptyTitle,
  emptyDescription,
  variant = "default",
  density = "default",
  layout = "default",
}) {
  const { t } = useSettings();

  if (!opportunities.length) {
    return (
      <EmptyState
        title={emptyTitle || t("common.emptyTitle")}
        description={emptyDescription || t("common.emptyDescription")}
      />
    );
  }

  return (
    <div className="opportunity-grid">
      {opportunities.map((opportunity) => (
        <OpportunityCard
          key={opportunity.id}
          opportunity={opportunity}
          onToggleSave={onToggleSave}
          variant={variant}
          density={density}
          layout={layout}
        />
      ))}
    </div>
  );
}
