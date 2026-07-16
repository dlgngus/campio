import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Avatar from "../components/common/Avatar.jsx";
import Badge from "../components/common/Badge.jsx";
import Button from "../components/common/Button.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import { mentorApi } from "../api/mentorApi.js";
import { isApiStatus } from "../api/client.js";
import { setAuthenticated } from "../app/authSession.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function MentorDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { language, labelCategory, t } = useSettings();
  const [mentor, setMentor] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;
    mentorApi.detail(id).then((item) => mounted && setMentor(item)).catch((err) => mounted && setError(err.message || t("common.errorDescription"))).finally(() => mounted && setLoading(false));
    mentorApi.myQuestions().then((items) => mounted && setQuestions(items.filter((item) => String(item.mentorId) === String(id)))).catch(() => {});
    return () => { mounted = false; };
  }, [id, t]);

  if (loading) return <LoadingSkeleton count={3} />;
  if (!mentor) return <EmptyState title={t("mentors.emptyTitle")} description={error || t("mentors.emptyDescription")} actionLabel={t("nav.mentors")} actionTo="/mentors" />;

  return <div className="page mentor-detail stack">
    <Link className="accent-link" to="/mentors">{language === "ko" ? "멘토 목록으로" : "Back to mentors"}</Link>
    <header className="mentor-detail__header"><Avatar src={mentor.avatarUrl} name={mentor.name} size="lg" /><div><p className="page-kicker">{[mentor.position, mentor.company].filter(Boolean).join(" · ")}</p><h1 className="page-title">{mentor.name}</h1><p>{[mentor.school, mentor.major].filter(Boolean).join(" · ")}</p></div><div className="mentor-detail__rate"><span>{language === "ko" ? "응답률" : "Response rate"}</span><strong>{mentor.responseRate}%</strong></div></header>
    <div className="status-list">{mentor.helpTopics.map((topic) => <Badge key={topic}>{labelCategory(topic)}</Badge>)}</div>
    <section className="copy-panel"><h2>{language === "ko" ? "경력과 소개" : "Experience"}</h2><p className="pre-wrap">{mentor.experience || "-"}</p></section>
    <section className="mentor-question-section"><h2>{language === "ko" ? "멘토에게 질문하기" : "Ask this mentor"}</h2><form className="form-grid" onSubmit={async (event) => { event.preventDefault(); if (!content.trim()) return; setSubmitting(true); setError(""); try { const question = await mentorApi.askQuestion(id, { content: content.trim() }); setQuestions((current) => [question, ...current]); setContent(""); } catch (err) { if (isApiStatus(err, 401)) { setAuthenticated(false); navigate("/login"); } else setError(err.message || t("common.errorDescription")); } finally { setSubmitting(false); } }}><textarea className="field__input field__textarea" value={content} maxLength={3000} placeholder={language === "ko" ? "준비 과정이나 지원 경험을 질문해보세요" : "Ask about preparation or application experience"} onChange={(event) => setContent(event.target.value)} /><Button type="submit" disabled={submitting || !content.trim()}>{language === "ko" ? "질문 보내기" : "Send question"}</Button></form></section>
    {questions.length ? <section className="mentor-questions"><h2>{language === "ko" ? "내 질문" : "My questions"}</h2>{questions.map((question) => <article key={question.id}><div><Badge>{question.status}</Badge><span>{question.createdAt}</span></div><p>{question.content}</p>{question.answer ? <blockquote>{question.answer}</blockquote> : <p className="soft">{language === "ko" ? "답변 대기 중" : "Waiting for an answer"}</p>}</article>)}</section> : null}
    {error ? <p className="form-error" role="alert">{error}</p> : null}
  </div>;
}
