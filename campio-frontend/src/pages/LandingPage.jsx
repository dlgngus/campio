import { ArrowRight, Bookmark, Search, Users } from "lucide-react";
import Button from "../components/common/Button.jsx";
import Card from "../components/common/Card.jsx";
import { useSettings } from "../app/settings.jsx";
import "./pages.css";

export default function LandingPage() {
  const { language } = useSettings();
  return (
    <main className="landing-page">
      <section className="landing-hero">
        <p className="page-kicker">Campio</p>
        <h1>{language === "ko" ? "대학생의 모든 기회를 한곳에서." : "Every student opportunity, in one place."}</h1>
        <p>{language === "ko" ? "인턴, 공모전, 장학금, 대외활동을 찾고 저장하고 지원 상태까지 관리하세요." : "Discover internships, contests, scholarships, and programs, then track every application."}</p>
        <div className="detail-actions">
          <Button to="/signup" icon={ArrowRight}>{language === "ko" ? "시작하기" : "Get started"}</Button>
          <Button to="/explore" variant="secondary" icon={Search}>{language === "ko" ? "기회 둘러보기" : "Explore"}</Button>
        </div>
      </section>
      <section className="landing-product-preview" aria-label="Campio workflow">
        <Card><Search aria-hidden="true" /><h2>{language === "ko" ? "탐색" : "Discover"}</h2><p>{language === "ko" ? "흩어진 공고를 한 번에 검색하세요." : "Search opportunities from trusted sources."}</p></Card>
        <Card><Bookmark aria-hidden="true" /><h2>{language === "ko" ? "저장과 추적" : "Save and track"}</h2><p>{language === "ko" ? "마감일과 지원 상태를 놓치지 마세요." : "Keep deadlines and application status together."}</p></Card>
        <Card><Users aria-hidden="true" /><h2>{language === "ko" ? "경험 공유" : "Learn together"}</h2><p>{language === "ko" ? "질문과 멘토 경험으로 더 나은 결정을 하세요." : "Use community and mentor context to decide."}</p></Card>
      </section>
    </main>
  );
}
