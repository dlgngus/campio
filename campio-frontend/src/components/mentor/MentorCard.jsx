import Badge from "../common/Badge.jsx";
import { useSettings } from "../../app/settings.jsx";
import "./mentor.css";

export default function MentorCard({ mentor }) {
  const { language, labelCategory } = useSettings();

  return (
    <article className="mentor-card">
      <div>
        <div className="mentor-card__avatar" aria-hidden="true">
          {mentor.name.slice(0, 1)}
        </div>
        <h3>{mentor.name}</h3>
        <p>
          {mentor.position} at {mentor.company}
        </p>
      </div>
      <p className="mentor-card__school">
        {mentor.school} · {mentor.major}
      </p>
      <div className="mentor-card__topics">
        {mentor.helpTopics.map((topic) => (
          <Badge key={topic}>{labelCategory(topic)}</Badge>
        ))}
      </div>
      <Badge tone={mentor.available ? "success" : "default"}>
        {mentor.available
          ? language === "ko"
            ? "상담 가능"
            : "Available"
          : language === "ko"
            ? "제한적"
            : "Limited"}
      </Badge>
    </article>
  );
}
