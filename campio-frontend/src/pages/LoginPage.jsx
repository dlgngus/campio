import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button.jsx";
import Input from "../components/common/Input.jsx";
import { authApi } from "../api/authApi.js";
import { setAuthenticated } from "../app/authSession.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function LoginPage() {
  const { t } = useSettings();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  return (
    <main className="public-main auth-page page">
      <section className="auth-card">
        <h1>{t("login.title")}</h1>
        <p>{t("login.copy")}</p>
        <form
          className="form-grid"
          onSubmit={async (event) => {
            event.preventDefault();
            setError("");
            setSubmitting(true);
            try {
              await authApi.login(form);
              setAuthenticated(true);
              navigate("/home");
            } catch (err) {
              setError(err.message || "Login failed");
            } finally {
              setSubmitting(false);
            }
          }}
        >
          <Input
            label={t("login.email")}
            name="email"
            type="email"
            placeholder="student@university.edu"
            value={form.email}
            onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
            required
          />
          <Input
            label={t("login.password")}
            name="password"
            type="password"
            placeholder={t("login.password")}
            value={form.password}
            onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
            required
          />
          {error ? <p className="form-error">{error}</p> : null}
          <Button type="submit" disabled={submitting}>
            {submitting ? t("login.loading") : t("login.action")}
          </Button>
          <Button to="/signup" variant="ghost">
            {t("login.create")}
          </Button>
        </form>
      </section>
    </main>
  );
}
