---
name: StromX
description: A calm, iOS-flavoured companion for one V-Strom SX, read at a glance on the handlebar.
colors:
  white: "#FFFFFF"
  silver-light: "#EDEEEB"
  heather-gray: "#CCC7BF"
  dark-night: "#31393C"
  electric-blue: "#2391FF"
  blue-text: "#1A62B0"
  slate: "#5E6669"
  blood: "#8A0303"
typography:
  family: "Geist (variable, bundled, SIL OFL)"
  large-title: { size: "34sp", weight: 700, lineHeight: "41sp" }
  title: { size: "22sp", weight: 700, lineHeight: "28sp" }
  headline: { size: "17sp", weight: 600, lineHeight: "22sp" }
  body: { size: "17sp", weight: 400, lineHeight: "22sp" }
  subhead: { size: "15sp", weight: 400 }
  footnote: { size: "13sp", weight: 400 }
  label: { size: "13sp", weight: 600, case: upper, tracking: "0.03em" }
  speed: { size: "104sp", weight: 600, tracking: "-0.04em", fontFeature: "tnum" }
  reading: { size: "28sp", weight: 600, fontFeature: "tnum" }
radius:
  card: "22dp"
  tile: "18dp"
  list: "14dp"
  control: "10dp"
spacing:
  gutter: "16dp"
  stack: "14dp"
  touch-min: "56dp"
---

# StromX design

Source of truth for the code: `app/src/main/java/com/adithyapaib/stromx/ui/theme/` (colours,
type, theme) and `ui/components/` (shared parts). The approved mockup is the "StromX Redesign"
design canvas.

## Feel

iOS grouped lists and large titles, built for a bike: one big number you can read in a
second, everything else in calm cards. Four tabs only: Ride, Trips, Garage, Settings.

## Colour

Five brand colours: white, silver light, heather gray, dark night and electric blue.
Derived shades exist only where contrast needs them.

| Role | Light | Black (AMOLED) |
|---|---|---|
| Background | silver light `#EDEEEB` | pure black `#000000` |
| Card | white | `#1A1E20` |
| Text | dark night `#31393C` | white |
| Secondary text | slate `#5E6669` | heather gray |
| Gauges and bars | dark night | electric blue |
| Accent text and icons | blue text `#1A62B0` | electric blue |
| Filled controls | electric blue | electric blue |

Rules:
- Electric blue is the only accent. Small text never sits in `#2391FF` on white, because it
  fails contrast; use `#1A62B0` there.
- White on electric blue only for large bold labels (19 sp bold buttons).
- Blood red `#8A0303` means one thing: the engine at 150 °C or above. The whole engine card
  fills red and the status says "Overheating · pull over", so colour is never the only signal.
- Appearance is Auto, Light or Black. Black is true `#000` for AMOLED.

## Type

Geist throughout, sized like iOS text styles. Every number that changes while riding uses
tabular figures. Upper case only for small card and section labels.

## Components

- Cards: rounded 22 dp (tiles 18 dp), no shadows, on the grouped background.
- Grouped lists: 14 dp inset sections with hairline dividers and footers that explain.
- Segmented control for Fuel/Service, Units and Appearance.
- Material switches tinted electric blue.
- Every tappable row or button is at least 56 dp tall, for gloves.

## Screens

- **Ride**: speed card, fuel and engine tiles, top speed and total, Find fuel and Log fill-up,
  the next service when due. Without the bike: a "Looking for your V-Strom" card and what the
  bike last reported.
- **Trips**: start and stop a ride, this month's totals, recent rides.
- **Garage**: Fuel (economy, cost, fill-ups) and Service (countdowns, mark done).
- **Settings**: bike, cluster greeting, appearance, units, always-on display, Maps turns,
  Diagnostics (the Bluetooth log, moved out of the tab bar).
- **Always-on ride display**: pure black, thin digits, the layout drifts a few dp each minute
  against burn-in, minimum brightness, double-tap to wake. Android doesn't let apps draw on the
  system always-on display, so StromX shows this itself instead of letting the screen sleep.

## Motion

Only state changes move: the engine card fades to red over 400 ms, the Bluetooth rings pulse
while scanning. No splash animation.
