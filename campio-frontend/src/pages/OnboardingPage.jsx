import { useState } from "react";
import Button from "../components/common/Button.jsx";
import FilterChip from "../components/common/FilterChip.jsx";
import Input from "../components/common/Input.jsx";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

const interests = [
  "Internship",
  "Contest",
  "Scholarship",
  "Exchange",
  "Startup",
  "External Activity",
  "Seminar",
  "Development",
  "Design",
  "Marketing",
  "Finance",
  "Research",
];

export default function OnboardingPage() {
  const { labelCategory, t } = useSettings();
  const [selected, setSelected] = useState(["Internship", "Development"]);

  function toggleInterest(item) {
    setSelected((current) =>
      current.includes(item) ? current.filter((value) => value !== item) : [...current, item]
    );
  }

  return (
    <main className="public-main onboarding-page page">
      <section className="auth-card">
        <h1>{t("onboarding.title")}</h1>
        <p>{t("onboarding.copy")}</p>
        <div className="form-grid">
          <Input label={t("signup.schoolEmail")} name="schoolEmail" placeholder="ryan@sunykorea.ac.kr" />
          <Input label={t("onboarding.code")} name="code" placeholder="123456" />
          <Input label={t("onboarding.school")} name="school" placeholder="SUNY Korea" />
          <Input label={t("onboarding.major")} name="major" placeholder="Computer Science" />
          <Input label={t("onboarding.grade")} name="grade" placeholder="3" />
          <div className="opportunity-filters__chips">
            {interests.map((item) => (
              <FilterChip key={item} selected={selected.includes(item)} onClick={() => toggleInterest(item)}>
                {labelCategory(item)}
              </FilterChip>
            ))}
          </div>
          <Button to="/home">{t("signup.continue")}</Button>
        </div>
      </section>
    </main>
  );
}
