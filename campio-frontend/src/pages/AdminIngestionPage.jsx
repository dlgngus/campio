import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "../api/authApi.js";
import { isApiStatus } from "../api/client.js";
import { ingestionApi } from "../api/ingestionApi.js";
import { setAuthenticated } from "../app/authSession.js";
import Button from "../components/common/Button.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import Input from "../components/common/Input.jsx";
import LoadingSkeleton from "../components/common/LoadingSkeleton.jsx";
import "./pages.css";

const initialSourceForm = {
  name: "",
  type: "RSS",
  baseUrl: "",
  categoryHint: "Government Support",
  crawlIntervalMinutes: 1440,
  robotsAllowed: true,
  enabled: false,
};

function sourcePayload(source) {
  return {
    name: source.name,
    type: source.type,
    baseUrl: source.baseUrl,
    categoryHint: source.categoryHint || "",
    crawlIntervalMinutes: Number(source.crawlIntervalMinutes || 1440),
    robotsAllowed: Boolean(source.robotsAllowed),
    enabled: Boolean(source.enabled),
  };
}

export default function AdminIngestionPage() {
  const navigate = useNavigate();
  const [sources, setSources] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [rawItems, setRawItems] = useState([]);
  const [form, setForm] = useState(initialSourceForm);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [runningSourceId, setRunningSourceId] = useState(null);
  const [selectedRaw, setSelectedRaw] = useState(null);
  const [publishForm, setPublishForm] = useState({ title: "", organization: "", category: "Internship", deadline: "", applyUrl: "" });
  const [requiresLogin, setRequiresLogin] = useState(false);
  const [error, setError] = useState("");

  const sourceSummary = useMemo(() => {
    const enabled = sources.filter((source) => source.enabled).length;
    return { enabled, total: sources.length };
  }, [sources]);

  async function loadAll(shouldUpdate = () => true) {
    setLoading(true);
    setError("");
    setRequiresLogin(false);
    try {
      const [me, sourceList, jobList, rawList] = await Promise.all([
        authApi.me(),
        ingestionApi.listSources(),
        ingestionApi.listCrawlJobs(),
        ingestionApi.listRawOpportunities(),
      ]);
      if (me.role !== "ADMIN") {
        throw new Error("Admin access required");
      }
      if (shouldUpdate()) {
        setSources(sourceList);
        setJobs(jobList);
        setRawItems(rawList);
      }
    } catch (err) {
      if (!shouldUpdate()) return;
      if (isApiStatus(err, 401)) {
        setAuthenticated(false);
        setRequiresLogin(true);
      } else {
        setError(err.message || "Failed to load ingestion data");
      }
    } finally {
      if (shouldUpdate()) setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    loadAll(() => mounted);
    return () => {
      mounted = false;
    };
  }, []);

  async function createSource(event) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      await ingestionApi.createSource(sourcePayload(form));
      setForm(initialSourceForm);
      await loadAll();
    } catch (err) {
      setError(err.message || "Failed to create source");
    } finally {
      setSubmitting(false);
    }
  }

  async function updateSource(source, patch) {
    setSubmitting(true);
    setError("");
    try {
      await ingestionApi.updateSource(source.id, sourcePayload({ ...source, ...patch }));
      await loadAll();
    } catch (err) {
      setError(err.message || "Failed to update source");
    } finally {
      setSubmitting(false);
    }
  }

  async function runSource(sourceId) {
    setRunningSourceId(sourceId);
    setError("");
    try {
      const job = await ingestionApi.createCrawlJob(sourceId);
      await ingestionApi.runCrawlJob(job.id);
      await loadAll();
    } catch (err) {
      setError(err.message || "Failed to run crawl job");
    } finally {
      setRunningSourceId(null);
    }
  }

  function beginRawReview(item) {
    const source = sources.find((candidate) => candidate.id === item.sourceId);
    setSelectedRaw(item);
    setPublishForm({
      title: item.rawTitle || "",
      organization: source?.name || "",
      category: source?.categoryHint || "Internship",
      deadline: "",
      applyUrl: item.sourceUrl || "",
    });
  }

  async function publishRaw() {
    if (!selectedRaw || !publishForm.title.trim() || !publishForm.category.trim() || !publishForm.deadline) return;
    setSubmitting(true);
    setError("");
    try {
      await ingestionApi.publishRawOpportunity(selectedRaw.id, {
        ...publishForm,
        description: selectedRaw.rawContent || "",
        deadline: publishForm.deadline,
        requirements: null,
        benefits: null,
        target: null,
        startDate: null,
        endDate: null,
        location: "Nationwide",
        isOnline: false,
        thumbnailUrl: null,
        tags: [],
        recommended: false,
        newThisWeek: true,
      });
      setSelectedRaw(null);
      await loadAll();
    } catch (err) {
      setError(err.message || "Failed to publish raw record");
    } finally {
      setSubmitting(false);
    }
  }

  async function rejectRaw(item) {
    setSubmitting(true);
    setError("");
    try {
      await ingestionApi.updateRawStatus(item.id, { status: "REJECTED", normalizedOpportunityId: null, errorMessage: "Rejected by admin" });
      if (selectedRaw?.id === item.id) setSelectedRaw(null);
      await loadAll();
    } catch (err) {
      setError(err.message || "Failed to reject raw record");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <p className="page-kicker">Admin ingestion pipeline</p>
          <h1 className="page-title">Crawling Sources.</h1>
        </div>
        <div className="detail-actions">
          <Button variant="secondary" onClick={() => loadAll()} disabled={loading || submitting || runningSourceId !== null}>
            Refresh
          </Button>
          <Button to="/admin/opportunities" variant="ghost">
            Opportunities
          </Button>
        </div>
      </header>

      {loading ? (
        <LoadingSkeleton count={4} />
      ) : requiresLogin ? (
        <EmptyState
          title="Login required."
          description="Sign in with an admin account to manage crawling sources."
          actionLabel="Go to login"
          onAction={() => navigate("/login")}
        />
      ) : error ? (
        <EmptyState title="Ingestion error." description={error} actionLabel="Retry" onAction={() => loadAll()} />
      ) : (
        <>
          <section className="ingestion-summary" aria-label="Ingestion summary">
            <div className="summary-card">
              <span>Sources</span>
              <strong>{sourceSummary.total}</strong>
            </div>
            <div className="summary-card">
              <span>Enabled</span>
              <strong>{sourceSummary.enabled}</strong>
            </div>
            <div className="summary-card">
              <span>Raw records</span>
              <strong>{rawItems.length}</strong>
            </div>
            <div className="summary-card">
              <span>Last job</span>
              <strong>{jobs[0]?.status || "None"}</strong>
            </div>
          </section>

          <div className="admin-layout">
            <section className="copy-panel">
              <h2>Add source</h2>
              <form className="form-grid" onSubmit={createSource}>
                <Input label="Name" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} required />
                <label className="field">
                  <span className="field__label">Type</span>
                  <select className="field__input" value={form.type} onChange={(event) => setForm((current) => ({ ...current, type: event.target.value }))}>
                    <option value="API">API</option>
                    <option value="YOUTH_POLICY_API">온통청년 API</option>
                    <option value="WORK24_API">고용24 API</option>
                    <option value="RSS">RSS</option>
                    <option value="HTML">HTML</option>
                  </select>
                </label>
                <Input label="Base URL" value={form.baseUrl} onChange={(event) => setForm((current) => ({ ...current, baseUrl: event.target.value }))} required />
                <Input label="Category hint" value={form.categoryHint} onChange={(event) => setForm((current) => ({ ...current, categoryHint: event.target.value }))} />
                <Input
                  label="Interval minutes"
                  type="number"
                  min="60"
                  value={form.crawlIntervalMinutes}
                  onChange={(event) => setForm((current) => ({ ...current, crawlIntervalMinutes: event.target.value }))}
                />
                <label className="field-check">
                  <input
                    type="checkbox"
                    checked={form.robotsAllowed}
                    onChange={(event) => setForm((current) => ({ ...current, robotsAllowed: event.target.checked }))}
                  />
                  <span>Robots allowed</span>
                </label>
                <label className="field-check">
                  <input
                    type="checkbox"
                    checked={form.enabled}
                    onChange={(event) => setForm((current) => ({ ...current, enabled: event.target.checked }))}
                  />
                  <span>Enable after save</span>
                </label>
                <Button type="submit" disabled={submitting}>
                  {submitting ? "Saving..." : "Create source"}
                </Button>
              </form>
            </section>

            <section className="ingestion-stack">
              <div className="copy-panel">
                <h2>Sources</h2>
                <div className="admin-table admin-table--ingestion">
                  <div className="admin-row admin-row--head">
                    <span>Name</span>
                    <span>Type</span>
                    <span>Enabled</span>
                    <span>Last crawl</span>
                    <span>Actions</span>
                  </div>
                  {sources.map((source) => (
                    <div className="admin-row" key={source.id}>
                      <span title={source.baseUrl}>{source.name}</span>
                      <span>{source.type}</span>
                      <span>{source.enabled ? "Yes" : "No"}</span>
                      <span>{source.lastCrawledAt ? new Date(source.lastCrawledAt).toLocaleString() : "Never"}</span>
                      <span className="inline-actions">
                        <button type="button" className="button button--secondary" disabled={submitting} onClick={() => updateSource(source, { enabled: !source.enabled })}>
                          {source.enabled ? "Disable" : "Enable"}
                        </button>
                        <button type="button" className="button button--primary" disabled={runningSourceId !== null || !source.enabled || !source.robotsAllowed} title={!source.enabled || !source.robotsAllowed ? "Enable the source and confirm robots permission first" : "Run source now"} onClick={() => runSource(source.id)}>
                          {runningSourceId === source.id ? "Running..." : "Run"}
                        </button>
                        <button
                          type="button"
                          className="button button--ghost"
                          disabled={submitting || runningSourceId !== null}
                          onClick={async () => {
                            if (!window.confirm(`Delete source "${source.name}" and its raw/job history?`)) return;
                            setSubmitting(true);
                            try {
                              await ingestionApi.deleteSource(source.id);
                              await loadAll();
                            } catch (err) {
                              setError(err.message || "Failed to delete source");
                            } finally {
                              setSubmitting(false);
                            }
                          }}
                        >
                          Delete
                        </button>
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="copy-panel">
                <h2>Recent jobs</h2>
                <div className="admin-table admin-table--jobs">
                  <div className="admin-row admin-row--head">
                    <span>ID</span>
                    <span>Source</span>
                    <span>Status</span>
                    <span>Found</span>
                    <span>Error</span>
                  </div>
                  {jobs.slice(0, 8).map((job) => (
                    <div className="admin-row" key={job.id}>
                      <span>{job.id}</span>
                      <span>{job.sourceId}</span>
                      <span>{job.status}</span>
                      <span>{job.itemsFound ?? 0}</span>
                      <span>{job.errorMessage || "-"}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="copy-panel">
                <h2>Raw records</h2>
                <div className="admin-table admin-table--raw">
                  <div className="admin-row admin-row--head">
                    <span>Title</span>
                    <span>Status</span>
                    <span>Source</span>
                    <span>Fetched</span>
                    <span>Actions</span>
                  </div>
                  {rawItems.slice(0, 10).map((item) => (
                    <div className="admin-row" key={item.id}>
                      <span title={item.sourceUrl}>{item.rawTitle}</span>
                      <span>{item.status}</span>
                      <span>{item.sourceId}</span>
                      <span>{item.fetchedAt ? new Date(item.fetchedAt).toLocaleString() : "-"}</span>
                      <span className="inline-actions">
                        <button type="button" className="button button--secondary" onClick={() => beginRawReview(item)} disabled={item.status === "PUBLISHED"}>Review</button>
                        <button type="button" className="button button--ghost" onClick={() => rejectRaw(item)} disabled={submitting || item.status === "PUBLISHED"}>Reject</button>
                      </span>
                    </div>
                  ))}
                </div>
                {selectedRaw ? (
                  <form className="raw-review-form form-grid" onSubmit={(event) => { event.preventDefault(); publishRaw(); }}>
                    <h3>Review raw record #{selectedRaw.id}</h3>
                    <Input label="Title" value={publishForm.title} onChange={(event) => setPublishForm((current) => ({ ...current, title: event.target.value }))} required />
                    <Input label="Organization" value={publishForm.organization} onChange={(event) => setPublishForm((current) => ({ ...current, organization: event.target.value }))} />
                    <Input label="Category" value={publishForm.category} onChange={(event) => setPublishForm((current) => ({ ...current, category: event.target.value }))} required />
                    <Input label="Deadline" type="date" value={publishForm.deadline} onChange={(event) => setPublishForm((current) => ({ ...current, deadline: event.target.value }))} required />
                    <Input label="Apply URL" type="url" value={publishForm.applyUrl} onChange={(event) => setPublishForm((current) => ({ ...current, applyUrl: event.target.value }))} />
                    <div className="inline-actions">
                      <Button type="submit" disabled={submitting}>Publish</Button>
                      <Button type="button" variant="ghost" onClick={() => setSelectedRaw(null)}>Cancel</Button>
                    </div>
                  </form>
                ) : null}
              </div>
            </section>
          </div>
        </>
      )}
    </div>
  );
}
