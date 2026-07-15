import { Link } from "react-router-dom";
import "./common.css";

export default function Button({
  children,
  variant = "primary",
  icon: Icon,
  to,
  className = "",
  ...props
}) {
  const classes = `button button--${variant} ${className}`.trim();
  const content = (
    <>
      {Icon ? <Icon size={17} aria-hidden="true" /> : null}
      <span>{children}</span>
    </>
  );

  if (to) {
    return (
      <Link className={classes} to={to} {...props}>
        {content}
      </Link>
    );
  }

  return (
    <button className={classes} type={props.type || "button"} {...props}>
      {content}
    </button>
  );
}
