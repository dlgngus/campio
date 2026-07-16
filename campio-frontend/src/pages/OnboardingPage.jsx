import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button.jsx";
import FilterChip from "../components/common/FilterChip.jsx";
import Input from "../components/common/Input.jsx";
import { authApi } from "../api/authApi.js";
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
  const navigate = useNavigate();
  const [selected, setSelected] = useState(["Internship", "Development"]);
  const [form, setForm] = useState({ school: "", major: "", grade: "" });
  const [schoolEmail, setSchoolEmail] = useState("");
  const [verificationCode, setVerificationCode] = useState("");
  const [developmentCode, setDevelopmentCode] = useState("");
  const [verified, setVerified] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let active = true;
    authApi.me().then((user) => {
      if (!active) return;
      setSchoolEmail(user.email || "");
      setVerified(Boolean(user.verified));
    }).catch(() => navigate("/login"));
    return () => {
      active = false;
    };
  }, [navigate]);

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
        <form
          className="form-grid"
          onSubmit={async (event) => {
            event.preventDefault();
            if (!verified) {
              setError("학교 이메일 인증을 먼저 완료해 주세요.");
              return;
            }
            setError("");
            setSubmitting(true);
            try {
              await authApi.updateProfile({
                school: form.school.trim(),
                major: form.major.trim(),
                grade: form.grade ? Number(form.grade) : null,
              });
              await authApi.updateInterests({ interests: selected.join(",") });
              navigate("/home");
            } catch (err) {
              setError(err.message || t("common.errorDescription"));
            } finally {
              setSubmitting(false);
            }
          }}
        >
          <Input
            label={t("signup.schoolEmail")}
            name="schoolEmail"
            type="email"
            value={schoolEmail}
            onChange={(event) => setSchoolEmail(event.target.value)}
            disabled={verified}
            required
          />
          {!verified ? (
            <>
              <Button
                type="button"
                variant="secondary"
                disabled={submitting}
                onClick={async () => {
                  setSubmitting(true);
                  setError("");
                  try {
                    const challenge = await authApi.requestSchoolVerification({ schoolEmail });
                    setDevelopmentCode(challenge.developmentCode || "");
                  } catch (err) {
                    setError(err.message || t("common.errorDescription"));
                  } finally {
                    setSubmitting(false);
                  }
                }}
              >
                인증 코드 받기
              </Button>
              {developmentCode ? <p className="verification-hint">개발 인증 코드: {developmentCode}</p> : null}
              <Input
                label="인증 코드"
                name="verificationCode"
                inputMode="numeric"
                pattern="[0-9]{6}"
                maxLength="6"
                value={verificationCode}
                onChange={(event) => setVerificationCode(event.target.value.replace(/\D/g, ""))}
              />
              <Button
                type="button"
                variant="secondary"
                disabled={submitting || verificationCode.length !== 6}
                onClick={async () => {
                  setSubmitting(true);
                  setError("");
                  try {
                    await authApi.verifySchool({ code: verificationCode });
                    setVerified(true);
                    setDevelopmentCode("");
                  } catch (err) {
                    setError(err.message || t("common.errorDescription"));
                  } finally {
                    setSubmitting(false);
                  }
                }}
              >
                학교 이메일 인증
              </Button>
            </>
          ) : null}
          <Input
            label={t("onboarding.school")}
            name="school"
            placeholder="SUNY Korea"
            value={form.school}
            onChange={(event) => setForm((current) => ({ ...current, school: event.target.value }))}
          />
          <Input
            label={t("onboarding.major")}
            name="major"
            placeholder="Computer Science"
            value={form.major}
            onChange={(event) => setForm((current) => ({ ...current, major: event.target.value }))}
          />
          <Input
            label={t("onboarding.grade")}
            name="grade"
            type="number"
            min="1"
            max="8"
            placeholder="3"
            value={form.grade}
            onChange={(event) => setForm((current) => ({ ...current, grade: event.target.value }))}
          />
          <div className="opportunity-filters__chips">
            {interests.map((item) => (
              <FilterChip key={item} selected={selected.includes(item)} onClick={() => toggleInterest(item)}>
                {labelCategory(item)}
              </FilterChip>
            ))}
          </div>
          {error ? <p className="form-error">{error}</p> : null}
          <Button type="submit" disabled={submitting}>
            {submitting ? t("common.loading") : t("signup.continue")}
          </Button>
        </form>
      </section>
    </main>
  );
}
