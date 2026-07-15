import "./common.css";

export default function FilterChip({ children, selected = false, disabled = false, ...props }) {
  return (
    <button
      type="button"
      className={`filter-chip${selected ? " is-selected" : ""}`}
      disabled={disabled}
      {...props}
    >
      {selected ? <span aria-hidden="true" /> : null}
      {children}
    </button>
  );
}
