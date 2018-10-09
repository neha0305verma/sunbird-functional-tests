package org.sunbird.common.action;

import com.consol.citrus.context.TestContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.sunbird.common.util.Constant;
import org.sunbird.integration.test.common.BaseCitrusTestRunner;

/**
 * Created by rajatgupta on 09/10/18.
 */
public class SystemSettingUtil {

    public static final String TEMPLATE_DIR = "templates/systemsetting/create";
    private static String getCreateSystemSettingUrl(BaseCitrusTestRunner runner) {
        return runner.getLmsApiUriPath("/api/data/v1/system/settings/set", "/v1/system/setting/set");
    }

    public static void createSystemSetting(
            BaseCitrusTestRunner runner,
            TestContext testContext,
            String templateDir,
            String testName,
            String variable) {

        runner.http(
                builder ->
                        TestActionUtil.getPostRequestTestAction(
                                builder,
                                Constant.LMS_ENDPOINT,
                                templateDir,
                                testName,
                                getCreateSystemSettingUrl(runner),
                                Constant.REQUEST_JSON,
                                MediaType.APPLICATION_JSON.toString(),
                                TestActionUtil.getHeaders(false)));

        runner.http(
                builder ->
                        TestActionUtil.getExtractFromResponseTestAction(
                                testContext,
                                builder,
                                Constant.LMS_ENDPOINT,
                                HttpStatus.OK,
                                "$.result.id",
                                variable));
        runner.sleep(Constant.ES_SYNC_WAIT_TIME);
    }
}
