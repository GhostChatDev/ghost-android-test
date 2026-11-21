"""Portable sleep-stage data and hypnogram SVG renderer (Python 3.10+).

The seeded data is demonstration data, not a measurement or medical result.
"""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta
from enum import Enum
from html import escape
from pathlib import Path
from typing import Iterable


class Stage(str, Enum):
    AWAKE = "Awake"
    REM = "REM"
    CORE = "Core"
    DEEP = "Deep"


@dataclass(frozen=True)
class StageSegment:
    stage: Stage
    start_minute: int
    duration_minutes: int

    @property
    def end_minute(self) -> int:
        return self.start_minute + self.duration_minutes


@dataclass(frozen=True)
class SleepReport:
    segments: tuple[StageSegment, ...]
    total_minutes: int
    is_demo: bool = True

    def __post_init__(self) -> None:
        validate_segments(self.segments, expected_minutes=self.total_minutes)

    def minutes_by_stage(self) -> dict[Stage, int]:
        return {stage: sum(s.duration_minutes for s in self.segments if s.stage is stage)
                for stage in Stage}

    def score(self) -> int:
        asleep = self.total_minutes - self.minutes_by_stage()[Stage.AWAKE]
        return max(0, min(100, round(100 * asleep / max(self.total_minutes, 1))))

    def efficiency_percent(self) -> float:
        awake = self.minutes_by_stage()[Stage.AWAKE]
        return 100.0 * (self.total_minutes - awake) / self.total_minutes


def validate_segments(segments: Iterable[StageSegment], expected_minutes: int) -> None:
    rows = tuple(segments)
    if expected_minutes <= 0:
        raise ValueError("expected_minutes must be positive")
    if not rows:
        raise ValueError("hypnogram cannot be empty")
    cursor = 0
    for row in rows:
        if not isinstance(row.stage, Stage):
            raise ValueError("every segment must have a known sleep stage")
        if row.duration_minutes <= 0:
            raise ValueError("segment duration must be positive")
        if row.start_minute != cursor:
            raise ValueError(f"hypnogram has a gap or overlap at minute {cursor}")
        cursor = row.end_minute
    if cursor != expected_minutes:
        raise ValueError(f"segments total {cursor} minutes, expected {expected_minutes}")


def seeded_eight_hour_report() -> SleepReport:
    """Build a deterministic 8-hour sample; all values are explicitly demo data."""
    pattern = [
        (Stage.CORE, 14), (Stage.DEEP, 44), (Stage.CORE, 18), (Stage.REM, 26),
        (Stage.CORE, 26), (Stage.DEEP, 44), (Stage.CORE, 24), (Stage.REM, 26),
        (Stage.CORE, 30), (Stage.AWAKE, 13), (Stage.CORE, 28), (Stage.REM, 26),
        (Stage.CORE, 30), (Stage.REM, 26), (Stage.CORE, 26), (Stage.AWAKE, 13),
        (Stage.CORE, 22), (Stage.REM, 26), (Stage.CORE, 18),
    ]
    # Make stage totals exactly REM 130, Deep 88, Core 236, Awake 26 (480 total).
    pattern = [(stage, duration) for stage, duration in pattern if duration > 0]
    segments: list[StageSegment] = []
    cursor = 0
    for stage, duration in pattern:
        segments.append(StageSegment(stage, cursor, duration))
        cursor += duration
    # Fill the remaining duration with core sleep, preserving complete timeline.
    if cursor < 480:
        segments.append(StageSegment(Stage.CORE, cursor, 480 - cursor))
    elif cursor > 480:
        raise ValueError("seed pattern exceeds eight hours")
    report = SleepReport(tuple(segments), 480, is_demo=True)
    # Guard the published demonstration totals against accidental edits.
    expected = {Stage.REM: 130, Stage.DEEP: 88, Stage.CORE: 236, Stage.AWAKE: 26}
    if report.minutes_by_stage() != expected:
        raise ValueError(f"demo stage totals changed: {report.minutes_by_stage()}")
    return report


def render_hypnogram_svg(report: SleepReport, destination: str | Path) -> Path:
    """Write a dependency-free SVG hypnogram to destination."""
    width, height = 900, 390
    left, right, top, bottom = 110, 24, 48, 48
    plot_w, plot_h = width - left - right, height - top - bottom
    colors = {Stage.AWAKE: "#fb7185", Stage.REM: "#c084fc",
              Stage.CORE: "#38bdf8", Stage.DEEP: "#6366f1"}
    levels = {Stage.DEEP: 3, Stage.CORE: 2, Stage.REM: 1, Stage.AWAKE: 0}
    row_h = plot_h / 4
    parts = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width} {height}">',
             '<rect width="100%" height="100%" rx="24" fill="#100b20"/>',
             '<text x="28" y="32" fill="#f8fafc" font-size="20" font-family="sans-serif">Sleep stages (demo data)</text>']
    for stage, level in levels.items():
        y = top + level * row_h
        parts.append(f'<line x1="{left}" y1="{y}" x2="{width-right}" y2="{y}" stroke="#ffffff22"/>')
        parts.append(f'<text x="{left-12}" y="{y + row_h/2 + 5}" text-anchor="end" fill="#cbd5e1" font-size="14" font-family="sans-serif">{escape(stage.value)}</text>')
    for segment in report.segments:
        x = left + plot_w * segment.start_minute / report.total_minutes
        w = plot_w * segment.duration_minutes / report.total_minutes
        y = top + levels[segment.stage] * row_h + 5
        parts.append(f'<rect x="{x:.2f}" y="{y:.2f}" width="{max(w, 1):.2f}" height="{row_h-10:.2f}" rx="4" fill="{colors[segment.stage]}"/>')
    parts.append(f'<text x="{left}" y="{height-14}" fill="#94a3b8" font-size="12" font-family="sans-serif">Bedtime</text>')
    parts.append(f'<text x="{width-right}" y="{height-14}" text-anchor="end" fill="#94a3b8" font-size="12" font-family="sans-serif">Wake time · 8 hours</text>')
    parts.append('</svg>')
    path = Path(destination)
    path.write_text("\n".join(parts), encoding="utf-8")
    return path


def print_report(report: SleepReport) -> None:
    totals = report.minutes_by_stage()
    print("Seeded night (DEMO ONLY): " + ", ".join(f"{stage.value} {totals[stage]}m" for stage in Stage))
    print(f"Sleep score: {report.score()}/100 | Efficiency: {report.efficiency_percent():.1f}%")


if __name__ == "__main__":
    sample = seeded_eight_hour_report()
    print_report(sample)
    output = render_hypnogram_svg(sample, "hypnogram.svg")
    print(f"Hypnogram written to {output.resolve()}")
