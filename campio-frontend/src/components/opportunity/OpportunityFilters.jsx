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
  "Research",
  "Startup",
  "Seminar",
  "Mentoring",
];

export const regions = [
  "All",
  "서울특별시",
  "경기도",
  "인천광역시",
  "부산광역시",
  "대구광역시",
  "광주광역시",
  "대전광역시",
  "울산광역시",
  "세종특별자치시",
  "강원특별자치도",
  "충청북도",
  "충청남도",
  "전북특별자치도",
  "전라남도",
  "경상북도",
  "경상남도",
  "제주특별자치도",
  "Nationwide",
];

export const sortOptions = ["deadline", "popular", "latest", "title"];

export default function OpportunityFilters({ query, target, category, region, sortBy, deadlineOnly, onlineOnly, savedOnly, onChange }) {
  const { labelCategory, labelLocation, t } = useSettings();

  return (
    <aside className="opportunity-filters">
      <Input
        label={t("filters.search")}
        placeholder={t("filters.placeholder")}
        value={query}
        onChange={(event) => onChange({ query: event.target.value })}
      />
      <Input
        label={t("detail.target")}
        placeholder={t("filters.targetPlaceholder")}
        value={target}
        onChange={(event) => onChange({ target: event.target.value })}
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
        <p className="opportunity-filters__label">{t("filters.region")}</p>
        <div className="opportunity-filters__chips">
          {regions.map((item) => (
            <FilterChip
              key={item}
              selected={region === item}
              onClick={() => onChange({ region: item })}
            >
              {item === "All" ? labelCategory("All") : labelLocation(item)}
            </FilterChip>
          ))}
        </div>
      </div>
      <label className="field">
        <span className="field__label">{t("filters.sort")}</span>
        <select className="field__input" value={sortBy} onChange={(event) => onChange({ sortBy: event.target.value })}>
          {sortOptions.map((item) => (
            <option key={item} value={item}>
              {t(`sort.${item}`)}
            </option>
          ))}
        </select>
      </label>
      <div>
        <p className="opportunity-filters__label">{t("filters.quick")}</p>
        <div className="opportunity-filters__chips">
          <FilterChip selected={deadlineOnly} onClick={() => onChange({ deadlineOnly: !deadlineOnly })}>
            {t("filters.deadlineSoon")}
          </FilterChip>
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
