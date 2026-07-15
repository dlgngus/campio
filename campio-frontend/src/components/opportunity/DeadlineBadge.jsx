import Badge from "../common/Badge.jsx";
import { useSettings } from "../../app/settings.jsx";

export default function DeadlineBadge({ deadline, urgent = false, accent = false }) {
  const { language } = useSettings();
  const days = Math.ceil((new Date(deadline) - new Date()) / 86400000);
  const label =
    days < 0
      ? language === "ko"
        ? "마감"
        : "Closed"
      : days === 0
        ? language === "ko"
          ? "오늘 마감"
          : "Due today"
      : `D-${days}`;

  const accentClass =
    accent && days >= 0 && days <= 2
      ? `deadline-badge--${days === 0 ? "d0" : days === 1 ? "d1" : "d2"}`
      : days <= 7
        ? "deadline-badge--soon"
        : "deadline-badge--neutral";

  return (
    <Badge
      tone="default"
      className={[
        "deadline-badge",
        urgent ? "deadline-badge--urgent" : "",
        accentClass,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {label}
    </Badge>
  );
}
