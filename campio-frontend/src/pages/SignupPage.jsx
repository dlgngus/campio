import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Avatar from "../components/common/Avatar.jsx";
import Button from "../components/common/Button.jsx";
import Input from "../components/common/Input.jsx";
import { authApi } from "../api/authApi.js";
import { setAuthenticated } from "../app/authSession.js";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function SignupPage() {
  const { t } = useSettings();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "", avatarUrl: "" });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function handleAvatarChange(event) {
    const file = event.target.files?.[0];
    if (!file) {
      setForm((current) => ({ ...current, avatarUrl: "" }));
      return;
    }

    if (!file.type.startsWith("image/") || file.size > 1024 * 1024) {
      setError(t("signup.avatarError"));
      event.target.value = "";
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      setError("");
      setForm((current) => ({ ...current, avatarUrl: String(reader.result || "") }));
    };
    reader.onerror = () => setError(t("signup.avatarError"));
    reader.readAsDataURL(file);
  }

  return (
    <main className="public-main auth-page page">
      <section className="auth-card">
        <h1>{t("signup.title")}</h1>
        <p>{t("signup.copy")}</p>
        <form
          className="form-grid"
          onSubmit={async (event) => {
            event.preventDefault();
            setError("");
            setSubmitting(true);
            try {
              await authApi.signup(form);
              setAuthenticated(true);
              navigate("/onboarding");
            } catch (err) {
              setError(err.message || "Signup failed");
            } finally {
              setSubmitting(false);
            }
          }}
        >
          <Input
            label={t("signup.name")}
            name="name"
            placeholder="Alex Kim"
            value={form.name}
            onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
            required
          />
          <label className="avatar-picker">
            <span className="field__label">{t("signup.avatar")}</span>
            <div className="avatar-picker__row">
              <Avatar src={form.avatarUrl} name={form.name} size="lg" />
              <div>
                <input type="file" accept="image/*" onChange={handleAvatarChange} />
                <p>{t("signup.avatarHelp")}</p>
              </div>
            </div>
          </label>
          <Input
            label={t("signup.schoolEmail")}
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
            {submitting ? t("signup.loading") : t("signup.continue")}
          </Button>
          <Button to="/login" variant="ghost">
            {t("signup.haveAccount")}
          </Button>
        </form>
      </section>
    </main>
  );
}
