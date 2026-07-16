import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Avatar from "../components/common/Avatar.jsx";
import Badge from "../components/common/Badge.jsx";
import Button from "../components/common/Button.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import FilterChip from "../components/common/FilterChip.jsx";
import Input from "../components/common/Input.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import PostCard from "../components/community/PostCard.jsx";
import { applicationApi } from "../api/applicationApi.js";
import { authApi } from "../api/authApi.js";
import { communityApi } from "../api/communityApi.js";
import { mentorApi } from "../api/mentorApi.js";
import { opportunityApi } from "../api/opportunityApi.js";
import { savedApi } from "../api/savedApi.js";
import { isApiStatus } from "../api/client.js";
import { setAuthenticated } from "../app/authSession.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

const tabs = ["OVERVIEW", "APPLICATIONS", "POSTS", "MENTOR_INBOX"];
const statuses = ["INTERESTED", "PREPARING", "APPLIED", "ACCEPTED", "REJECTED", "ARCHIVED"];

export default function ProfilePage() {
  const { language, labelCategory, labelStatus, t } = useSettings();
  const navigate = useNavigate();
  const [tab, setTab] = useState("OVERVIEW");
  const [user, setUser] = useState(null);
  const [savedCount, setSavedCount] = useState(0);
  const [applications, setApplications] = useState([]);
  const [opportunities, setOpportunities] = useState([]);
  const [posts, setPosts] = useState([]);
  const [inbox, setInbox] = useState([]);
  const [answers, setAnswers] = useState({});
  const [form, setForm] = useState({ name: "", school: "", major: "", grade: "", interests: "" });
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [requiresLogin, setRequiresLogin] = useState(false);

  async function loadProfile(active = () => true) {
    setLoading(true); setError(""); setRequiresLogin(false);
    try {
      const [me, saved, records, myPosts] = await Promise.all([
        authApi.me(), savedApi.list(), applicationApi.list(), communityApi.myPosts(),
      ]);
      const all = await opportunityApi.batch(records.map((record) => record.opportunityId));
      let received = [];
      try { received = await mentorApi.receivedQuestions(); } catch (err) { if (!isApiStatus(err, 403) && !isApiStatus(err, 404)) throw err; }
      if (!active()) return;
      setUser(me); setSavedCount(saved.length); setApplications(records); setOpportunities(all); setPosts(myPosts); setInbox(received);
      setForm({ name: me.name || "", school: me.school || "", major: me.major || "", grade: me.grade || "", interests: me.interests || "" });
    } catch (err) {
      if (!active()) return;
      if (isApiStatus(err, 401)) { setAuthenticated(false); setRequiresLogin(true); }
      else setError(err.message || t("common.errorDescription"));
    } finally { if (active()) setLoading(false); }
  }

  useEffect(() => { let mounted = true; loadProfile(() => mounted); return () => { mounted = false; }; }, [t]);

  const opportunityById = useMemo(() => new Map(opportunities.map((item) => [item.id, item])), [opportunities]);
  const tabLabel = (value) => ({
    OVERVIEW: language === "ko" ? "프로필" : "Profile",
    APPLICATIONS: language === "ko" ? "지원 현황" : "Applications",
    POSTS: language === "ko" ? "내 글" : "My posts",
    MENTOR_INBOX: language === "ko" ? "멘토 질문함" : "Mentor inbox",
  })[value];

  if (loading) return <LoadingSkeleton count={4} />;
  if (requiresLogin) return <EmptyState title={t("common.loginRequiredTitle")} description={t("common.loginRequiredDescription")} actionLabel={t("common.goLogin")} onAction={() => navigate("/login")} />;
  if (error || !user) return <EmptyState title={t("common.errorTitle")} description={error || t("common.errorDescription")} actionLabel={t("common.retry")} onAction={() => loadProfile()} />;

  return <div className="page profile-page stack">
    <header className="page-header"><div className="profile-heading"><Avatar src={user.avatarUrl} name={user.name} size="lg" /><div><p className="page-kicker">{t("profile.kicker")}</p><h1 className="page-title">{user.name}</h1><p className="muted">{user.email}{user.verified ? ` · ${language === "ko" ? "학교 인증 완료" : "Verified"}` : ""}</p></div></div>{tab === "OVERVIEW" ? <Button variant="secondary" onClick={() => setEditing((current) => !current)}>{editing ? (language === "ko" ? "취소" : "Cancel") : (language === "ko" ? "프로필 편집" : "Edit profile")}</Button> : null}</header>
    <nav className="status-list" aria-label={language === "ko" ? "내 정보 메뉴" : "Profile sections"}>{tabs.map((item) => <FilterChip key={item} selected={tab === item} onClick={() => setTab(item)}>{tabLabel(item)}{item === "MENTOR_INBOX" && inbox.length ? ` ${inbox.length}` : ""}</FilterChip>)}</nav>

    {tab === "OVERVIEW" ? <>
      {editing ? <form className="profile-edit-form form-grid" onSubmit={async (event) => { event.preventDefault(); setSaving(true); setError(""); try { const updated = await authApi.updateProfile({ name: form.name.trim(), school: form.school.trim(), major: form.major.trim(), grade: form.grade ? Number(form.grade) : null }); const withInterests = await authApi.updateInterests({ interests: form.interests.trim() }); setUser({ ...updated, interests: withInterests.interests }); setEditing(false); } catch (err) { setError(err.message || t("common.errorDescription")); } finally { setSaving(false); } }}><Input label={t("signup.name")} value={form.name} maxLength={100} required onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} /><Input label={t("onboarding.school")} value={form.school} maxLength={100} onChange={(event) => setForm((current) => ({ ...current, school: event.target.value }))} /><Input label={t("onboarding.major")} value={form.major} maxLength={100} onChange={(event) => setForm((current) => ({ ...current, major: event.target.value }))} /><Input label={t("onboarding.grade")} type="number" min="1" max="8" value={form.grade} onChange={(event) => setForm((current) => ({ ...current, grade: event.target.value }))} /><Input label={language === "ko" ? "관심사 (쉼표로 구분)" : "Interests (comma separated)"} value={form.interests} required onChange={(event) => setForm((current) => ({ ...current, interests: event.target.value }))} /><Button type="submit" disabled={saving}>{saving ? t("common.loading") : (language === "ko" ? "변경 저장" : "Save changes")}</Button></form> : <><div className="simple-grid"><div className="copy-panel"><h2>{t("profile.school")}</h2><p>{user.school || "-"}</p></div><div className="copy-panel"><h2>{t("profile.major")}</h2><p>{user.major || "-"} · {t("onboarding.grade")} {user.grade || "-"}</p></div><div className="copy-panel"><h2>{t("profile.saved")}</h2><p>{savedCount} {t("home.saved")}</p></div></div><section><h2 className="profile-section-title">{t("profile.interests")}</h2><div className="status-list">{(user.interests ? user.interests.split(",").map((item) => item.trim()).filter(Boolean) : []).map((item) => <Badge key={item}>{labelCategory(item)}</Badge>)}</div></section></>}
    </> : null}

    {tab === "APPLICATIONS" ? <section className="profile-list"><h2>{tabLabel(tab)}</h2>{applications.length ? applications.map((record) => { const opportunity = opportunityById.get(record.opportunityId); return <article key={record.id}><div><strong>{opportunity?.title || `${language === "ko" ? "기회" : "Opportunity"} #${record.opportunityId}`}</strong><span>{opportunity?.organization}</span></div><select className="field__input" value={record.status} aria-label={language === "ko" ? "지원 상태" : "Application status"} onChange={async (event) => { const status = event.target.value; const previous = record.status; setApplications((current) => current.map((item) => item.id === record.id ? { ...item, status } : item)); try { await applicationApi.update(record.id, { status, memo: record.memo || "" }); } catch (err) { setApplications((current) => current.map((item) => item.id === record.id ? { ...item, status: previous } : item)); setError(err.message || t("common.errorDescription")); } }}>{statuses.map((status) => <option key={status} value={status}>{labelStatus(status)}</option>)}</select></article>; }) : <EmptyState title={language === "ko" ? "아직 지원 기록이 없어요." : "No applications yet."} description={language === "ko" ? "공고 상세에서 지원 상태를 추가하세요." : "Add a status from an opportunity detail page."} />}</section> : null}

    {tab === "POSTS" ? <section className="profile-list"><h2>{tabLabel(tab)}</h2>{posts.length ? <div className="simple-grid">{posts.map((post) => <PostCard key={post.id} post={post} />)}</div> : <EmptyState title={t("community.emptyTitle")} description={t("community.emptyDescription")} />}</section> : null}

    {tab === "MENTOR_INBOX" ? <section className="profile-list"><h2>{tabLabel(tab)}</h2>{inbox.length ? inbox.map((question) => <article key={question.id} className="mentor-inbox-item"><div><strong>{question.questionerName}</strong><span>{question.createdAt} · {question.status}</span></div><p>{question.content}</p>{question.answer ? <blockquote>{question.answer}</blockquote> : <form className="profile-answer-form" onSubmit={async (event) => { event.preventDefault(); const answer = answers[question.id]?.trim(); if (!answer) return; try { const updated = await mentorApi.answerQuestion(question.id, { answer }); setInbox((current) => current.map((item) => item.id === question.id ? updated : item)); } catch (err) { setError(err.message || t("common.errorDescription")); } }}><textarea className="field__input field__textarea" value={answers[question.id] || ""} maxLength={3000} placeholder={language === "ko" ? "답변을 작성하세요" : "Write an answer"} onChange={(event) => setAnswers((current) => ({ ...current, [question.id]: event.target.value }))} /><Button type="submit">{language === "ko" ? "답변 등록" : "Answer"}</Button></form>}</article>) : <EmptyState title={language === "ko" ? "받은 질문이 없어요." : "No questions received."} description={language === "ko" ? "승인된 멘토에게 온 질문이 여기에 표시됩니다." : "Questions for approved mentors appear here."} />}</section> : null}
    {error ? <p className="form-error" role="alert">{error}</p> : null}
  </div>;
}
