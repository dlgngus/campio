import Badge from "../common/Badge.jsx";
import Avatar from "../common/Avatar.jsx";
import Button from "../common/Button.jsx";
import { useSettings } from "../../app/settings.jsx";
import "./mentor.css";

export default function MentorCard({ mentor }) {
  const { language, labelCategory } = useSettings();
  const name = mentor.name || (language === "ko" ? "Campio 멘토" : "Campio Mentor");
  const affiliation = [mentor.school, mentor.major].filter(Boolean).join(" · ");

  return (
    <article className="mentor-card">
      <div>
        <Avatar src={mentor.avatarUrl} name={name} />
        <h3>{name}</h3>
        <p>
          {mentor.position} at {mentor.company}
        </p>
      </div>
      {affiliation ? <p className="mentor-card__school">{affiliation}</p> : null}
      <div className="mentor-card__topics">
        {(mentor.helpTopics || []).map((topic) => (
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
      <Button to={`/mentors/${mentor.id}`} variant="secondary">{language === "ko" ? "멘토 상세" : "View mentor"}</Button>
    </article>
  );
}
