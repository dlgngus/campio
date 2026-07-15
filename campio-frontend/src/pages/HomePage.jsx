import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search } from "lucide-react";
import Card from "../components/common/Card.jsx";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function HomePage() {
  const { language, labelCategory, t } = useSettings();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState("");
  const categories = ["Internship", "Competition", "Scholarship", "Exchange", "Research"];

  function handleSearchSubmit(event) {
    event.preventDefault();
    const query = searchQuery.trim();
    navigate(query ? `/explore?q=${encodeURIComponent(query)}` : "/explore");
  }

  const heroTitle =
    language === "ko" ? (
      <>
        나에게 딱 맞는 <span className="home-hero-accent">커리어</span>를 찾아보세요.
      </>
    ) : (
      <>
        Find the <span className="home-hero-accent">career</span> that fits you.
      </>
    );

  return (
    <div className="page home-page">
      <section className="home-site-hero">
        <div className="home-site-hero__pattern" aria-hidden="true">
          <svg viewBox="0 0 1440 720" role="presentation" focusable="false">
            <defs>
              <pattern id="home-grid" width="72" height="72" patternUnits="userSpaceOnUse">
                <path d="M72 0H0V72" fill="none" stroke="#111111" strokeOpacity="0.035" strokeWidth="1" />
              </pattern>
            </defs>
            <rect width="1440" height="720" fill="url(#home-grid)" />
            <path
              d="M-80 540C140 444 312 442 498 502C680 563 848 574 1050 500C1194 447 1308 386 1540 354"
              fill="none"
              stroke="#111111"
              strokeOpacity="0.025"
              strokeWidth="1.2"
              strokeLinecap="round"
            />
            <path
              d="M-100 208C94 124 284 120 448 170C640 230 812 244 1000 194C1158 150 1302 92 1520 92"
              fill="none"
              stroke="#111111"
              strokeOpacity="0.02"
              strokeWidth="1.2"
              strokeLinecap="round"
            />
          </svg>
        </div>

        <div className="home-section-shell home-site-hero__inner">
          <div className="home-site-hero__copy home-site-hero__copy--center">
            <p className="page-kicker">{t("home.kicker")}</p>
            <h1>{heroTitle}</h1>
            <p>{t("home.heroCopy")}</p>
            <form className="home-search-card home-search-card--center" onSubmit={handleSearchSubmit} role="search">
              <Search size={19} aria-hidden="true" />
              <input
                aria-label={t("filters.search")}
                placeholder={t("home.searchPlaceholder")}
                value={searchQuery}
                onChange={(event) => setSearchQuery(event.target.value)}
              />
            </form>
            <div className="home-category-strip home-category-strip--center" aria-label={t("home.categories")}>
              {categories.map((category) => (
                <button
                  type="button"
                  className="home-category-chip"
                  key={category}
                  onClick={() => navigate(`/explore?category=${encodeURIComponent(category)}`)}
                >
                  {labelCategory(category)}
                </button>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="home-feature-cards">
        <div className="home-section-shell">
          <div className="home-feature-cards__grid">
            <Card className="home-feature-card">
              <div className="home-feature-card__icon">🔍</div>
              <h2>{language === "ko" ? "기회 탐색" : "Opportunity Search"}</h2>
              <p>인턴십, 공모전, 장학금 정보를 한곳에서 간결하게 확인하세요.</p>
            </Card>
            <Card className="home-feature-card">
              <div className="home-feature-card__icon">👥</div>
              <h2>{language === "ko" ? "커뮤니티" : "Community"}</h2>
              <p>다양한 사람들과 정보와 커리어를 공유하고 소통해 보세요.</p>
            </Card>
            <Card className="home-feature-card">
              <div className="home-feature-card__icon">👨‍🏫</div>
              <h2>{language === "ko" ? "멘토링" : "Mentoring"}</h2>
              <p>현업 전문가와 멘토들에게 직접 피드백을 받고 성장하세요.</p>
            </Card>
          </div>
        </div>
      </section>

      <section className="home-cta">
        <div className="home-section-shell home-cta__inner">
          <p>{t("home.homeCtaCopy")}</p>
          <button type="button" className="button button--primary" onClick={() => navigate("/explore")}>
            {t("home.homeCtaAction")}
          </button>
        </div>
      </section>
    </div>
  );
}
