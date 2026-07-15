import { User } from "lucide-react";
import "./common.css";

export default function Avatar({ src, name, size = "md", className = "" }) {
  const initials = (name || "")
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();

  return (
    <span className={`avatar avatar--${size} ${className}`.trim()} aria-hidden="true">
      {src ? <img src={src} alt="" /> : initials ? <span>{initials}</span> : <User size={size === "lg" ? 26 : 18} />}
    </span>
  );
}
