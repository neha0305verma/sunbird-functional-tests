package org.sunbird.kp.test.util;

import com.consol.citrus.context.TestContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.sunbird.kp.test.common.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility Class for Itemset API Test
 *
 */

public class ItemsetUtil {
    private static final String API_KEY = AppConfig.config.getString("kp_api_key");
    private static final Boolean IS_USER_AUTH_REQUIRED = AppConfig.config.getBoolean("user_auth_enable");
    private static List<String> AS_API_LIST = AppConfig.config.getStringList("as_api_list");
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final String ITEMSET_PAYLOAD_DIR = "templates/payload/itemset";

    public static Map<String, Object> createItemset(BaseCitrusTestRunner runner, TestContext testContext, String payload, String payloadName, Map<String, Object> headers) {
        Map<String, Object> data = new HashMap<String, Object>();
        runner.variable("idVal", String.valueOf(System.currentTimeMillis()));
        if (StringUtils.isNotBlank(payload)) {
            System.out.println("Dynamic Payload...");
            runner.http(
                    builder ->
                            TestActionUtil.getPostRequestTestAction(
                                    builder,
                                    getEndPoint(APIUrl.CREATE_ITEMSET),
                                    APIUrl.CREATE_ITEMSET,
                                    MediaType.APPLICATION_JSON.toString(),
                                    payload,
                                    getHeaders(headers)));

        } else if(StringUtils.isNotBlank(payloadName)){
            System.out.println("Static Payload Used With Dynamic Values...");
            runner.http(
                    builder ->
                            TestActionUtil.processPostRequest(
                                    builder,
                                    getEndPoint(APIUrl.CREATE_ITEMSET),
                                    ITEMSET_PAYLOAD_DIR,
                                    payloadName,
                                    APIUrl.CREATE_ITEMSET,
                                    Constant.REQUEST_JSON,
                                    Constant.CONTENT_TYPE_APPLICATION_JSON,
                                    getHeaders(headers)));
        }
        runner.http(
                builder ->
                        TestActionUtil.getExtractFromResponseTestAction(
                                testContext,
                                builder,
                                getEndPoint(APIUrl.CREATE_ITEMSET),
                                HttpStatus.OK,
                                "$.result",
                                "result"));
        Map<String, Object> result = getResult(testContext);
        if (MapUtils.isNotEmpty(result)) {
            data.put("itemset_id", (String) result.get("identifier"));
        }
        return data;
    }

    /**
     * This Method provides all necessary header elements
     *
     * @param additionalHeaders
     * @return
     */
    private static Map<String, Object> getHeaders(Map<String, Object> additionalHeaders) {
        Map<String, Object> headers = new HashMap<String, Object>();
        if (MapUtils.isNotEmpty(additionalHeaders))
            headers.putAll(additionalHeaders);

        if (!headers.containsKey(Constant.X_CHANNEL_ID))
            headers.put(Constant.X_CHANNEL_ID, AppConfig.config.getString("kp_test_default_channel"));

        headers.put(Constant.AUTHORIZATION, Constant.BEARER + API_KEY);
        headers.put(Constant.X_CONSUMER_ID,  UUID.randomUUID().toString());

        if (IS_USER_AUTH_REQUIRED)
            headers.put(Constant.X_AUTHENTICATED_USER_TOKEN, "${accessToken}");
        return headers;
    }

    private static String getEndPoint(String reqUrl) {
        return AS_API_LIST.contains(reqUrl) ? Constant.KP_ASSESSMENT_SERVICE_ENDPOINT : Constant.KP_ENDPOINT;
    }

    /**
     * This Method Returns Result Map
     *
     * @param testContext
     * @return
     */
    private static Map<String, Object> getResult(TestContext testContext) {
        Map<String, Object> result = null;
        try {
            result = objectMapper.readValue(testContext.getVariable("result"), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            System.out.println("Exception Occurred While parsing context variable : " + e);
        }
        return result;
    }
}
