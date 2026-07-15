import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import Input from "../components/common/Input.jsx";
import { useSettings } from "../app/settings.jsx";
import { setAuthenticated } from "../app/authSession.js";
import { isApiStatus } from "../api/client.js";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import { authApi } from "../api/authApi.js";
import { opportunityApi } from "../api/opportunityApi.js";
import "./pages.css";

export default function AdminOpportunitiesPage() {
  const { labelCategory, t } = useSettings();
  const navigate = useNavigate();
  const [opportunities, setOpportunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [requiresLogin, setRequiresLogin] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const [form, setForm] = useState({
    title: "",
    organization: "",
    category: "Internship",
    deadline: "",
    location: "",
    status: "PUBLISHED",
    applyUrl: "",
    description: "",
    requirements: "",
    benefits: "",
    target: "",
    recommended: false,
    newThisWeek: false,
  });

  async function loadOpportunities(shouldUpdate = () => true) {
    setLoading(true);
    setError("");
    setRequiresLogin(false);
    try {
      const [me, items] = await Promise.all([authApi.me(), opportunityApi.list()]);
      if (me.role !== "ADMIN") {
        throw new Error("Admin access required");
      }
      if (shouldUpdate()) setOpportunities(items);
    } catch (err) {
      if (!shouldUpdate()) return;
      if (isApiStatus(err, 401)) {
        setAuthenticated(false);
        setRequiresLogin(true);
      } else {
        setError(err.message || t("common.errorDescription"));
      }
    } finally {
      if (shouldUpdate()) setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    loadOpportunities(() => mounted);
    return () => {
      mounted = false;
    };
  }, [t]);

  function beginEdit(opportunity) {
    setSelectedId(opportunity.id);
    setForm({
      title: opportunity.title || "",
      organization: opportunity.organization || "",
      category: opportunity.category || "Internship",
      deadline: opportunity.deadline || "",
      location: opportunity.location || "",
      status: opportunity.status || "PUBLISHED",
      applyUrl: opportunity.applyUrl || "",
      description: opportunity.description || "",
      requirements: opportunity.requirements || "",
      benefits: opportunity.benefits || "",
      target: opportunity.target || "",
      recommended: Boolean(opportunity.recommended),
      newThisWeek: Boolean(opportunity.newThisWeek),
    });
  }

  function resetForm() {
    setSelectedId(null);
    setForm({
      title: "",
      organization: "",
      category: "Internship",
      deadline: "",
      location: "",
      status: "PUBLISHED",
      applyUrl: "",
      description: "",
      requirements: "",
      benefits: "",
      target: "",
      recommended: false,
      newThisWeek: false,
    });
  }

  async function submitForm(event) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    const payload = {
      ...form,
      deadline: form.deadline || null,
      startDate: null,
      endDate: null,
      isOnline: form.location.toLowerCase().includes("online"),
      tags: [],
      popularityCount: 0,
    };
    try {
      if (selectedId) {
        await opportunityApi.update(selectedId, payload);
      } else {
        await opportunityApi.create(payload);
      }
      const items = await opportunityApi.list();
      setOpportunities(items);
      resetForm();
    } catch (err) {
      if (isApiStatus(err, 401)) {
        setAuthenticated(false);
        setRequiresLogin(true);
      } else {
        setError(err.message || t("common.errorDescription"));
      }
    } finally {
      setSubmitting(false);
    }
  }

  async function removeOpportunity(id) {
    setSubmitting(true);
    setError("");
    try {
      await opportunityApi.remove(id);
      const items = await opportunityApi.list();
      setOpportunities(items);
      if (selectedId === id) {
        resetForm();
      }
    } catch (err) {
      if (isApiStatus(err, 401)) {
        setAuthenticated(false);
        setRequiresLogin(true);
      } else {
        setError(err.message || t("common.errorDescription"));
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <p className="page-kicker">{t("admin.kicker")}</p>
          <h1 className="page-title">{t("admin.title")}</h1>
        </div>
        <Button onClick={resetForm}>{t("admin.create")}</Button>
      </header>
      <div className="admin-layout">
        <section className="copy-panel">
          <h2>{selectedId ? "Edit Opportunity" : "Create Opportunity"}</h2>
          <form className="form-grid" onSubmit={submitForm}>
            <Input label="Title" value={form.title} onChange={(e) => setForm((c) => ({ ...c, title: e.target.value }))} required />
            <Input label="Organization" value={form.organization} onChange={(e) => setForm((c) => ({ ...c, organization: e.target.value }))} />
            <Input label="Category" value={form.category} onChange={(e) => setForm((c) => ({ ...c, category: e.target.value }))} />
            <Input label="Deadline" type="date" value={form.deadline} onChange={(e) => setForm((c) => ({ ...c, deadline: e.target.value }))} />
            <Input label="Location" value={form.location} onChange={(e) => setForm((c) => ({ ...c, location: e.target.value }))} />
            <Input label="Apply URL" value={form.applyUrl} onChange={(e) => setForm((c) => ({ ...c, applyUrl: e.target.value }))} />
            <Input label="Target" value={form.target} onChange={(e) => setForm((c) => ({ ...c, target: e.target.value }))} />
            <Input label="Status" value={form.status} onChange={(e) => setForm((c) => ({ ...c, status: e.target.value }))} />
            <Input label="Description" value={form.description} onChange={(e) => setForm((c) => ({ ...c, description: e.target.value }))} />
            <Input label="Requirements" value={form.requirements} onChange={(e) => setForm((c) => ({ ...c, requirements: e.target.value }))} />
            <Input label="Benefits" value={form.benefits} onChange={(e) => setForm((c) => ({ ...c, benefits: e.target.value }))} />
            <label className="field">
              <span className="field__label">Recommended</span>
              <input type="checkbox" checked={form.recommended} onChange={(e) => setForm((c) => ({ ...c, recommended: e.target.checked }))} />
            </label>
            <label className="field">
              <span className="field__label">New this week</span>
              <input type="checkbox" checked={form.newThisWeek} onChange={(e) => setForm((c) => ({ ...c, newThisWeek: e.target.checked }))} />
            </label>
            {error ? <p className="form-error">{error}</p> : null}
            <div style={{ display: "flex", gap: 12 }}>
              <Button type="submit" disabled={submitting}>{submitting ? "Saving..." : selectedId ? "Save changes" : "Create opportunity"}</Button>
              <Button variant="ghost" type="button" onClick={resetForm}>
                Reset
              </Button>
            </div>
          </form>
        </section>

        <section>
          {loading ? (
            <LoadingSkeleton count={4} />
          ) : requiresLogin ? (
            <EmptyState
              title={t("common.loginRequiredTitle")}
              description={t("common.loginRequiredDescription")}
              actionLabel={t("common.goLogin")}
              onAction={() => navigate("/login")}
            />
          ) : error ? (
            <EmptyState
              title={t("common.errorTitle")}
              description={error}
              actionLabel={t("common.retry")}
              onAction={() => loadOpportunities()}
            />
          ) : (
            <div className="admin-table">
              <div className="admin-row admin-row--head">
                <span>{t("admin.colTitle")}</span>
                <span>{t("admin.colCategory")}</span>
                <span>{t("admin.colOrganization")}</span>
                <span>{t("admin.colDeadline")}</span>
                <span>{t("admin.colStatus")}</span>
                <span>Actions</span>
              </div>
              {opportunities.slice(0, 8).map((opportunity) => (
                <div className="admin-row" key={opportunity.id}>
                  <span>{opportunity.title}</span>
                  <span>{labelCategory(opportunity.category)}</span>
                  <span>{opportunity.organization}</span>
                  <span>{opportunity.deadline}</span>
                  <span>{opportunity.status || t("admin.published")}</span>
                  <span style={{ display: "flex", gap: 8 }}>
                    <button className="button button--secondary" type="button" onClick={() => beginEdit(opportunity)}>
                      Edit
                    </button>
                    <button className="button button--ghost" type="button" disabled={submitting} onClick={() => removeOpportunity(opportunity.id)}>
                      Delete
                    </button>
                  </span>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
