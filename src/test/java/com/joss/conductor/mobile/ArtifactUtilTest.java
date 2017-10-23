package com.joss.conductor.mobile;

import com.joss.conductor.mobile.util.ArtifactUtil;
import com.joss.conductor.mobile.util.ScreenShotUtil;
import com.joss.conductor.mobile.util.WaitUtil;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

/**
 * Created on 1/24/17.
 */
public class ArtifactUtilTest {

    @Test
    void artifact_path_starts_with_default_path() {
        String testName = "test_name";
        String filePath = ArtifactUtil.artifactPathForTest(testName, "");

        Assertions.assertThat(filePath.startsWith(ArtifactUtil.WORKING_DIR)).isTrue();
    }

    @Test
    void artifact_path_ends_with_extension() {
        String testName = "test_name";
        String filePath = ArtifactUtil.artifactPathForTest(testName, ".png");

        Assertions.assertThat(filePath.endsWith(".png")).isTrue();
    }

    @Test
    void artifact_file_matches_test_name_with_timestamp() {
        String test = "test_name";
        String filePath = ArtifactUtil.artifactPathForTest(test, ".png");

        String artifactFileName = Paths.get(filePath).getFileName().toString();
        String[] parts = artifactFileName.split("-");
        String testPart = parts[0];
        String datePart = parts[1];

        Assertions.assertThat(testPart)
                .isEqualTo(test);
        try {
            Assertions.assertThat(ArtifactUtil.SDF.parse(datePart))
                    .isToday();
        } catch (ParseException e) {
            Assert.fail("Exception thrown:" + e.toString());
        }
    }

    @Test
    public void test_file_name_creation_is_unique() {
        String path = "Name of the Test";
        String testName = "Error message of the test";

        String filepath1 = ArtifactUtil.artifactPathForTest(path, testName);
        WaitUtil.wait(100, "Waiting " + 100 + " millis");
        String filepath2 = ArtifactUtil.artifactPathForTest(path, testName);

        Assertions.assertThat(filepath1)
                .isNotEqualTo(filepath2);
    }

    @Test
    public void test_file_name_less_than_100_characters() {
        String path = "name_of_test_name_of_test_name_of_test_name_of_test_name_of_test_name_of_test_";

        String filePath = ArtifactUtil.artifactPathForTest(path, "");

        String retPath = Paths.get(filePath).getFileName().toString();
        Assertions.assertThat(retPath.length())
                .isEqualTo(100);
    }

    @Test
    public void test_with_path_returns_path_filename_timestamp() {
        String path = "path_for_this_artifact";
        String testName = "name_of_test";

        String filePath = ArtifactUtil.artifactPathForTest(path, testName, ".png");

        Path retFilePath = Paths.get(filePath);
        String retFile = retFilePath.getFileName().toString();
        String[] fileParts = retFile.split("-");
        String retPath = retFilePath.getName(retFilePath.getNameCount() - 2).toString();

        Assertions.assertThat(fileParts[0])
                .isEqualTo(testName);
        try {
            Assertions.assertThat(ArtifactUtil.SDF.parse(fileParts[1]))
                    .isToday();
        } catch (ParseException e) {
            Assert.fail("Exception thrown:" + e.toString());
        }
        Assertions.assertThat(retPath)
                .isEqualTo(path);
    }

    @Test
    public void test_file_name_sanitizes_special_characters() {
        String path = "test_with:_?*s<p>ec\"ial||_/cha\\racters";

        String filePath = ArtifactUtil.artifactPathForTest(path, "");

        String retPath = Paths.get(filePath).getFileName().toString();
        retPath = retPath.substring(0, retPath.indexOf('-'));
        Assertions.assertThat(retPath)
                .isEqualTo("test_with+_++s+p+ec+ial++_+cha+racters");
    }

    @Test
    public void test_file_name_and_path_creation_is_less_than_100_chars() {
        String path = "name_of_test_name_of_test_name_of_test_name_of_test_name_of_test_name_of_test_longy_longy_longy_long_long_long";
        String testName = "error_message_of_test_error_message_of_test_error_message_of_test_error_message_of_test_more_long_than_120_characters_why";

        String filePath = ArtifactUtil.artifactPathForTest(path, testName, "");

        Path retFilePath = Paths.get(filePath);
        String retFile = retFilePath.getFileName().toString();
        String[] fileParts = retFile.split("-");
        String retPath = retFilePath.getName(retFilePath.getNameCount() - 2).toString();

        Assertions.assertThat(fileParts[0].length())
                .isLessThanOrEqualTo(100);
        Assertions.assertThat(retPath.length())
                .isLessThanOrEqualTo(100);
    }

    @Test
    public void test_file_name_and_path_creation_sanitizes_file_and_path() {
        String path = "more_:_?*spe<c>ial\"||_/charac\\ters";
        String testName = "test_with:_?*s<p>ec\"ial||_/cha\\racters";

        String filePath = ArtifactUtil.artifactPathForTest(path, testName, "");

        Path retFilePath = Paths.get(filePath);
        String retFile = retFilePath.getFileName().toString();
        String[] fileParts = retFile.split("-");
        String retPath = retFilePath.getName(retFilePath.getNameCount() - 2).toString();

        Assertions.assertThat(retPath)
                .isEqualTo("more_+_++spe+c+ial+++_+charac+ters");
        Assertions.assertThat(fileParts[0])
                .isEqualTo("test_with+_++s+p+ec+ial++_+cha+racters");
    }

    @Test
    public void test_names_sanitize_null_parameters() {
        String path = null;

        String firstPath = ArtifactUtil.artifactPathForTest(null, "");
        String secondPath = ArtifactUtil.artifactPathForTest("testName", null, "");
        String thirdPath = ArtifactUtil.artifactPathForTest("testName", "test", null);

        Assertions.assertThat(Paths.get(firstPath).getFileName().toString())
                .contains("(null)");
        Assertions.assertThat(Paths.get(secondPath).getFileName().toString())
                .contains("(null)");
        Assertions.assertThat(Paths.get(thirdPath).getFileName().toString())
                .doesNotContain("null");
    }
}
