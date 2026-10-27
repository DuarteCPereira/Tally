The headline value of a screen: a small label, the amount in the `display` style, and a signed change underneath.

Use it once per screen. It is the largest and boldest thing on the page and everything else stays quieter. The label uses `caption` in `text-secondary`. The change uses `label` in `positive` or `negative` and always starts with `+` or `-`, so meaning never depends on colour alone.

**Planned Compose API**: `DuiAmountText(label, amount, change, trend = Trend.Up)`. Amount and change arrive as already formatted strings.
