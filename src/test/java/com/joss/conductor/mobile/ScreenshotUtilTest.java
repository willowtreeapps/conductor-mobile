package com.joss.conductor.mobile;

import com.joss.conductor.mobile.util.ScreenShotUtil;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.File;

/**
 * Created on 1/24/17.
 */
public class ScreenshotUtilTest {

    @Test
    public void test_file_name_creation_is_unique() {
        String path = "Name of the Test";
        String testName = "Error message of the test";

        String filepath1 = ScreenShotUtil.createFilePathAndName(path, testName);
        String filepath2 = ScreenShotUtil.createFilePathAndName(path, testName);

        Assertions.assertThat(filepath1)
                .isNotEqualTo(filepath2);
    }

    @Test
    public void test_file_name_and_path_creation_is_less_than_120_chars() {
        String path = "name_of_test_name_of_test_name_of_test_name_of_test_name_of_test_name_of_test_";
        String testName = "error_message_of_test_error_message_of_test_error_message_of_test_error_message_of_test_";

        String filepath = ScreenShotUtil.createFilePathAndName(path, testName);
        Assertions.assertThat(new File(filepath).getName().length())
                .isLessThanOrEqualTo(120);
        Assertions.assertThat(new File(new File(filepath).getParent()).getName().length())
                .isLessThanOrEqualTo(120);
    }

}
