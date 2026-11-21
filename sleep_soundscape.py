"""Portable sleep-soundscape presets and playback timer controls (Python 3.10+).

This module models the controls and preset configuration. Actual audio playback is
platform-specific; this Python file intentionally does not claim to drive iOS audio.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from enum import Enum
from time import monotonic


class Preset(str, Enum):
    DEEP_SPACE_DRIFT = "Deep Space Drift"
    REM_STARLIGHT = "REM Starlight"
    NEBULA_RAIN = "Nebula Rain"
    WARM_TONE = "432Hz Warm Tone"


@dataclass
class Soundscape:
    preset: Preset = Preset.DEEP_SPACE_DRIFT
    drone_volume: float = 0.45
    delta_volume: float = 0.35
    theta_volume: float = 0.0
    rain_volume: float = 0.2
    timer_minutes: int | None = 30
    playing: bool = False
    _started_at: float | None = field(default=None, repr=False)

    def __post_init__(self) -> None:
        self._validate_volumes()
        if self.timer_minutes is not None and self.timer_minutes not in (15, 30, 45, 60):
            raise ValueError("timer_minutes must be 15, 30, 45, 60, or None")

    def _validate_volumes(self) -> None:
        for name in ("drone_volume", "delta_volume", "theta_volume", "rain_volume"):
            value = getattr(self, name)
            if not 0.0 <= value <= 1.0:
                raise ValueError(f"{name} must be between 0 and 1")

    def apply_preset(self, preset: Preset) -> None:
        self.preset = preset
        settings = {
            Preset.DEEP_SPACE_DRIFT: (0.48, 0.42, 0.0, 0.18),
            Preset.REM_STARLIGHT: (0.30, 0.0, 0.42, 0.12),
            Preset.NEBULA_RAIN: (0.22, 0.0, 0.0, 0.68),
            Preset.WARM_TONE: (0.58, 0.0, 0.0, 0.08),
        }[preset]
        self.drone_volume, self.delta_volume, self.theta_volume, self.rain_volume = settings

    def start(self, now: float | None = None) -> None:
        self._started_at = monotonic() if now is None else now
        self.playing = True

    def stop(self) -> None:
        self.playing = False
        self._started_at = None

    def volume_multiplier(self, now: float | None = None) -> float:
        """Return linear timer fade: full volume, then fade during final 5 minutes."""
        if not self.playing or self.timer_minutes is None or self._started_at is None:
            return 1.0 if self.playing else 0.0
        current = monotonic() if now is None else now
        elapsed = max(0.0, current - self._started_at)
        duration = self.timer_minutes * 60.0
        remaining = duration - elapsed
        if remaining <= 0:
            self.stop()
            return 0.0
        return min(1.0, remaining / min(300.0, duration))

    def tick(self, now: float | None = None) -> bool:
        """Update timer; returns True when playback is still active."""
        self.volume_multiplier(now)
        return self.playing
