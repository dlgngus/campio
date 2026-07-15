import "./common.css";

export default function Card({ as: Element = "div", className = "", children, ...props }) {
  return (
    <Element className={`card ${className}`.trim()} {...props}>
      {children}
    </Element>
  );
}
