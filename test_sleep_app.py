"""Logic tests for portable sleep report and soundscape modules. Run: python -m unittest -v"""
import tempfile
import unittest
from pathlib import Path

from sleep_tracker import (
    SleepReport, Stage, StageSegment, render_hypnogram_svg,
    seeded_eight_hour_report, validate_segments,
)
from sleep_soundscape import Preset, Soundscape


class SleepTrackerTests(unittest.TestCase):
    def test_seeded_hypnogram_has_eight_hours_and_all_four_stages(self):
        report = seeded_eight_hour_report()
        self.assertEqual(report.total_minutes, 480)
        self.assertEqual(report.minutes_by_stage(), {
            Stage.AWAKE: 26, Stage.REM: 130, Stage.CORE: 236, Stage.DEEP: 88,
        })

    def test_empty_hypnogram_is_rejected(self):
        with self.assertRaisesRegex(ValueError, "cannot be empty"):
            SleepReport((), 480)

    def test_gaps_and_wrong_totals_are_rejected(self):
        with self.assertRaisesRegex(ValueError, "gap or overlap"):
            validate_segments((StageSegment(Stage.CORE, 1, 30),), 30)
        with self.assertRaisesRegex(ValueError, "expected 60"):
            validate_segments((StageSegment(Stage.CORE, 0, 30),), 60)

    def test_svg_chart_renders_all_stage_labels_and_data(self):
        report = seeded_eight_hour_report()
        with tempfile.TemporaryDirectory() as folder:
            path = render_hypnogram_svg(report, Path(folder) / "chart.svg")
            contents = path.read_text(encoding="utf-8")
        for label in ("Awake", "REM", "Core", "Deep", "demo data"): 
            self.assertIn(label, contents)
        self.assertGreaterEqual(contents.count("<rect "), 20)


class SoundscapeTests(unittest.TestCase):
    def test_presets_select_expected_mix(self):
        sound = Soundscape()
        sound.apply_preset(Preset.REM_STARLIGHT)
        self.assertEqual(sound.theta_volume, 0.42)
        self.assertEqual(sound.delta_volume, 0.0)
        sound.apply_preset(Preset.NEBULA_RAIN)
        self.assertEqual(sound.rain_volume, 0.68)

    def test_timer_fades_then_stops(self):
        sound = Soundscape(timer_minutes=15)
        sound.start(now=100.0)
        self.assertEqual(sound.volume_multiplier(now=100.0), 1.0)
        self.assertAlmostEqual(sound.volume_multiplier(now=850.0), 0.5)
        self.assertFalse(sound.tick(now=1000.0))
        self.assertEqual(sound.volume_multiplier(now=1000.0), 0.0)

    def test_invalid_volume_rejected(self):
        with self.assertRaisesRegex(ValueError, "between 0 and 1"):
            Soundscape(drone_volume=1.2)


if __name__ == "__main__":
    unittest.main()
