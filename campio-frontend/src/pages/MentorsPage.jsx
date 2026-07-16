import { useEffect, useState } from "react";
import { BriefcaseBusiness } from "lucide-react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button.jsx";
import Input from "../components/common/Input.jsx";
import SectionHeader from "../components/common/SectionHeader.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import MentorCard from "../components/mentor/MentorCard.jsx";
import { mentorApi } from "../api/mentorApi.js";
import { authApi } from "../api/authApi.js";
import { useSettings } from "../app/settings.jsx";
import { isApiStatus } from "../api/client.js";
import { setAuthenticated } from "../app/authSession.js";
import "./pages.css";

export default function MentorsPage() {
  const { language, t } = useSettings();
  const navigate = useNavigate();
  const [mentors, setMentors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [applying, setApplying] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [applyForm, setApplyForm] = useState({ company: "", position: "", experience: "", helpTopics: "" });
  const [applicationMessage, setApplicationMessage] = useState("");
  const [requiresLogin, setRequiresLogin] = useState(false);
  const [requiresVerification, setRequiresVerification] = useState(false);

  async function loadMentors(shouldUpdate = () => true) {
    setLoading(true);
    setError("");
    setRequiresLogin(false);
    setRequiresVerification(false);
    try {
      const me = await authApi.me();
      if (!me.verified) {
        if (shouldUpdate()) setRequiresVerification(true);
        return;
      }
      const items = await mentorApi.list();
      if (shouldUpdate()) setMentors(items);
    } catch (err) {
      if (!shouldUpdate()) return;
      if (isApiStatus(err, 401)) {
        setAuthenticated(false);
        setRequiresLogin(true);
      } else if (isApiStatus(err, 403)) {
        setRequiresVerification(true);
      } else {
        setError(err.message || t("common.errorDescription"));
      }
    } finally {
      if (shouldUpdate()) setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    loadMentors(() => mounted);
    return () => {
      mounted = false;
    };
  }, [t]);

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <p className="page-kicker">{t("mentors.kicker")}</p>
          <h1 className="page-title">{t("mentors.title")}</h1>
        </div>
        {!requiresLogin && !requiresVerification ? <Button icon={BriefcaseBusiness} variant="secondary" onClick={() => setApplying((current) => !current)}>{language === "ko" ? "멘토 지원" : "Apply as mentor"}</Button> : null}
      </header>
      {applying ? <form className="mentor-apply-form form-grid" onSubmit={async (event) => {
        event.preventDefault(); setSubmitting(true); setError(""); setApplicationMessage("");
        try {
          await mentorApi.apply({ ...applyForm, helpTopics: applyForm.helpTopics.split(",").map((item) => item.trim()).filter(Boolean) });
          setApplicationMessage(language === "ko" ? "지원이 접수됐습니다. 관리자 승인 후 공개됩니다." : "Application received. Your profile will appear after approval.");
          setApplying(false);
        } catch (err) {
          if (isApiStatus(err, 401)) { setAuthenticated(false); navigate("/login"); }
          else setError(err.message || t("common.errorDescription"));
        } finally { setSubmitting(false); }
      }}>
        <Input label={language === "ko" ? "회사·기관" : "Company"} value={applyForm.company} maxLength={150} required onChange={(event) => setApplyForm((current) => ({ ...current, company: event.target.value }))} />
        <Input label={language === "ko" ? "직무·직책" : "Position"} value={applyForm.position} maxLength={150} required onChange={(event) => setApplyForm((current) => ({ ...current, position: event.target.value }))} />
        <label className="field"><span className="field__label">{language === "ko" ? "경력 및 소개" : "Experience"}</span><textarea className="field__input field__textarea" value={applyForm.experience} maxLength={5000} onChange={(event) => setApplyForm((current) => ({ ...current, experience: event.target.value }))} /></label>
        <Input label={language === "ko" ? "도움 분야 (쉼표로 구분)" : "Help topics (comma separated)"} value={applyForm.helpTopics} onChange={(event) => setApplyForm((current) => ({ ...current, helpTopics: event.target.value }))} />
        <div className="detail-actions"><Button type="submit" disabled={submitting}>{submitting ? t("common.loading") : (language === "ko" ? "지원 제출" : "Submit")}</Button><Button variant="ghost" onClick={() => setApplying(false)}>{language === "ko" ? "취소" : "Cancel"}</Button></div>
      </form> : null}
      {applicationMessage ? <p className="success-message" role="status">{applicationMessage}</p> : null}
      <SectionHeader title={t("mentors.available")} />
      {loading ? (
        <LoadingSkeleton count={3} />
      ) : requiresLogin ? (
        <EmptyState title={t("common.loginRequiredTitle")} description={language === "ko" ? "멘토 서비스는 로그인 후 이용할 수 있습니다." : "Sign in to use the mentor service."} actionLabel={t("common.goLogin")} onAction={() => navigate("/login")} />
      ) : requiresVerification ? (
        <EmptyState title={language === "ko" ? "대학생 인증이 필요합니다" : "Student verification required"} description={language === "ko" ? "학교 이메일 인증을 완료한 대학생만 멘토 서비스를 이용할 수 있습니다." : "Verify your university email to access mentors."} actionLabel={language === "ko" ? "학교 인증하기" : "Verify school email"} onAction={() => navigate("/onboarding")} />
      ) : error ? (
        <EmptyState
          title={t("common.errorTitle")}
          description={error}
          actionLabel={t("common.retry")}
          onAction={() => loadMentors()}
        />
      ) : !mentors.length ? (
        <EmptyState title={t("mentors.emptyTitle")} description={t("mentors.emptyDescription")} />
      ) : (
        <div className="simple-grid">
          {mentors.map((mentor) => (
            <MentorCard key={mentor.id} mentor={mentor} />
          ))}
        </div>
      )}
    </div>
  );
}
