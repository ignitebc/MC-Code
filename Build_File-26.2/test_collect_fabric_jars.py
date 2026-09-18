import importlib.util
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch


SPEC = importlib.util.spec_from_file_location(
    "collector", Path(__file__).with_name("collect_fabric_jars.py")
)
collector = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(collector)


class CollectionTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.base = self.root / "Build_File"
        self.base.mkdir()
        self.output = self.base / "build_files"
        self.output.mkdir()
        (self.output / "old.jar").write_bytes(b"last successful release")
        self.source = self.root / "new.jar"
        self.source.write_bytes(b"new release")
        self.releases = [("module", "testmod", self.source)]
        self.file_patch = patch.object(collector, "__file__", str(self.base / "collect_fabric_jars.py"))
        self.file_patch.start()
        self.addCleanup(self.file_patch.stop)

    def assert_old_release(self):
        self.assertEqual((self.output / "old.jar").read_bytes(), b"last successful release")
        self.assertFalse((self.output / "new.jar").exists())

    def test_prepare_preserves_previous_release(self):
        self.assertTrue(collector.prepare_target_directory(self.output))
        self.assert_old_release()

    def test_unexpected_target_is_rejected(self):
        self.assertFalse(collector.publish_release_jars(self.releases, self.root / "elsewhere"))
        self.assert_old_release()

    def test_copy_failure_preserves_release(self):
        with patch.object(collector.shutil, "copy2", side_effect=OSError("copy failed")):
            self.assertFalse(collector.publish_release_jars(self.releases, self.output))
        self.assert_old_release()

    def test_publish_failure_restores_release(self):
        rename = Path.rename

        def fail_new(path, target):
            if path.name == "new":
                raise OSError("publish failed")
            return rename(path, target)

        with patch.object(Path, "rename", fail_new):
            self.assertFalse(collector.publish_release_jars(self.releases, self.output))
        self.assert_old_release()

    def test_failed_restore_preserves_backup(self):
        rename = Path.rename

        def fail_new_and_restore(path, target):
            if path.name in ("new", "previous"):
                raise OSError("rename failed")
            return rename(path, target)

        with patch.object(Path, "rename", fail_new_and_restore):
            self.assertFalse(collector.publish_release_jars(self.releases, self.output))
        backups = list(self.base.glob(".jar-collection-*/previous/old.jar"))
        self.assertEqual(len(backups), 1)
        self.assertEqual(backups[0].read_bytes(), b"last successful release")

    def test_success_replaces_whole_output(self):
        self.assertTrue(collector.publish_release_jars(self.releases, self.output))
        self.assertEqual((self.output / "new.jar").read_bytes(), b"new release")
        self.assertFalse((self.output / "old.jar").exists())
        self.assertEqual(list(self.base.glob(".jar-collection-*")), [])

    def test_duplicate_filename_keeps_previous_output(self):
        self.assertFalse(collector.publish_release_jars(self.releases * 2, self.output))
        self.assert_old_release()

    def test_build_failure_preserves_release(self):
        (self.root / "module").mkdir()
        (self.base / collector.DEPENDENCY_MANIFEST_NAME).write_text("dependencies", encoding="utf-8")
        with patch.object(collector, "MODULES", [("module", None, "testmod")]), \
                patch.object(collector, "find_required_java_home", return_value=self.root), \
                patch.object(collector, "build_fabric_module", return_value=False):
            self.assertEqual(collector.copy_module_jars(), 1)
        self.assert_old_release()


if __name__ == "__main__":
    unittest.main()
