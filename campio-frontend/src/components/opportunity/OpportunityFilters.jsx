import FilterChip from "../common/FilterChip.jsx";
import Input from "../common/Input.jsx";
import { useSettings } from "../../app/settings.jsx";
import "./opportunity.css";

export const categories = [
  "All",
  "Internship",
  "Contest",
  "External Activity",
  "Scholarship",
  "Exchange",
  "Startup",
  "Seminar",
  "Mentoring",
];

export default function OpportunityFilters({ query, category, onlineOnly, savedOnly, onChange }) {
  const { labelCategory, t } = useSettings();

  return (
    <aside className="opportunity-filters">
      <Input
        label={t("filters.search")}
        placeholder={t("filters.placeholder")}
        value={query}
        onChange={(event) => onChange({ query: event.target.value })}
      />
      <div>
        <p className="opportunity-filters__label">{t("filters.category")}</p>
        <div className="opportunity-filters__chips">
          {categories.map((item) => (
            <FilterChip
              key={item}
              selected={category === item}
              onClick={() => onChange({ category: item })}
            >
              {labelCategory(item)}
            </FilterChip>
          ))}
        </div>
      </div>
      <div>
        <p className="opportunity-filters__label">{t("filters.quick")}</p>
        <div className="opportunity-filters__chips">
          <FilterChip selected={onlineOnly} onClick={() => onChange({ onlineOnly: !onlineOnly })}>
            {t("filters.online")}
          </FilterChip>
          <FilterChip selected={savedOnly} onClick={() => onChange({ savedOnly: !savedOnly })}>
            {t("filters.saved")}
          </FilterChip>
        </div>
      </div>
    </aside>
  );
}
