# Colors

Use these CSS variables.

```css
:root {
  --color-canvas: #050505;
  --color-surface-1: #101010;
  --color-surface-2: #181818;
  --color-surface-3: #222222;

  --color-ink: #ffffff;
  --color-ink-muted: #b3b3b3;
  --color-ink-soft: #7a7a7a;

  --color-accent-blue: #3b82f6;

  --color-hairline: rgba(255, 255, 255, 0.12);
  --color-hairline-soft: rgba(255, 255, 255, 0.07);

  --color-success: #38d996;
  --color-warning: #ffb020;
  --color-danger: #ff5c5c;

  --gradient-violet: linear-gradient(135deg, #6e38ff 0%, #d946ef 100%);
  --gradient-orange: linear-gradient(135deg, #ff8a00 0%, #ff3d6e 100%);
  --gradient-coral: linear-gradient(135deg, #ff4f8b 0%, #ff7a59 100%);
}
```

## Usage Rules

### Canvas

Use `--color-canvas` for:
- body background
- page background
- FAQ-style rows
- footer

### Surface 1

Use `--color-surface-1` for:
- regular cards
- secondary buttons
- inputs
- opportunity cards

### Surface 2

Use `--color-surface-2` for:
- featured cards
- active tabs
- highlighted panels

### Accent Blue

Only use blue for:
- links
- focus ring
- selected chip
- active nav indicator

Never use blue as:
- primary CTA background
- large card background
- decorative section background
