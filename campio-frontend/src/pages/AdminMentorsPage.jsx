import { useEffect, useState } from "react";
import Button from "../components/common/Button.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import { mentorApi } from "../api/mentorApi.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function AdminMentorsPage() {
  const { language, t } = useSettings();
  const [mentors, setMentors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const load = () => { setLoading(true); setError(""); mentorApi.adminList().then(setMentors).catch((err) => setError(err.message || t("common.errorDescription"))).finally(() => setLoading(false)); };
  useEffect(load, [t]);
  return <div className="page"><header className="page-header"><div><p className="page-kicker">{language === "ko" ? "멘토 지원 검토" : "Mentor application review"}</p><h1 className="page-title">{language === "ko" ? "멘토 승인" : "Mentor approvals"}</h1></div></header>{loading ? <LoadingSkeleton count={4} /> : error ? <EmptyState title={t("common.errorTitle")} description={error} actionLabel={t("common.retry")} onAction={load} /> : <div className="admin-table admin-table--mentors"><div className="admin-row admin-row--head"><span>{language === "ko" ? "이름" : "Name"}</span><span>{language === "ko" ? "소속" : "Role"}</span><span>{language === "ko" ? "분야" : "Topics"}</span><span>{language === "ko" ? "상태" : "Status"}</span><span>{language === "ko" ? "작업" : "Action"}</span></div>{mentors.map((mentor) => <div className="admin-row" key={mentor.id}><span>{mentor.name}</span><span>{mentor.position} · {mentor.company}</span><span>{mentor.helpTopics.join(", ") || "-"}</span><span>{mentor.available ? (language === "ko" ? "승인" : "Approved") : (language === "ko" ? "대기" : "Pending")}</span><span><Button variant={mentor.available ? "ghost" : "secondary"} onClick={async () => { try { const updated = await mentorApi.setApproval(mentor.id, !mentor.available); setMentors((current) => current.map((item) => item.id === mentor.id ? updated : item)); } catch (err) { setError(err.message || t("common.errorDescription")); } }}>{mentor.available ? (language === "ko" ? "승인 취소" : "Revoke") : (language === "ko" ? "승인" : "Approve")}</Button></span></div>)}</div>}</div>;
}
