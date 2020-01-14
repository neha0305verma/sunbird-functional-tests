package org.sunbird.kp.test.common;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.HttpActionBuilder;
import com.consol.citrus.dsl.builder.HttpClientActionBuilder;
import com.consol.citrus.dsl.builder.HttpClientRequestActionBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.Assert;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Utility Class which perform rest calls
 *
 * @author Kumar Gauraw
 */
public class TestActionUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static List<String> objectTypes = Arrays.asList("content","framework","assessment","dialcode","definition","concept");

    /**
     *
     * @param builder
     * @param endpointName
     * @param userName
     * @param password
     * @return
     */
    public static TestAction getTokenRequest(
            HttpActionBuilder builder, String endpointName, String userName, String password) {
        return builder
                .client(endpointName)
                .send()
                .post(APIUrl.USER_AUTH)
                .contentType("application/x-www-form-urlencoded")
                .payload(
                        "client_id=admin-cli"
                                + "&username="
                                + userName
                                + "&password="
                                + password
                                + "&grant_type=password");
    }

    /**
     *
     * @param builder
     * @param endpointName
     * @return
     */
    public static TestAction getTokenResponse(
            HttpActionBuilder builder, String endpointName) {
        return builder
                .client(endpointName)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.JSON)
                .extractFromPayload("$.access_token", "accessToken");
    }

    /**
     *
     * @param builder
     * @param endpointName
     * @param testName
     * @param requestUrl
     * @param headers
     * @return
     */
    public static TestAction processGetRequest(
            HttpActionBuilder builder,
            String endpointName,
            String testName,
            String requestUrl,
            Map<String, Object> headers) {
        HttpClientRequestActionBuilder requestActionBuilder =
                builder.client(endpointName)
                        .send()
                        .get(requestUrl)
                        .messageType(MessageType.JSON);

        if (null != headers)
            requestActionBuilder = addHeaders(requestActionBuilder, headers);

        return requestActionBuilder;
    }

    /**
     *
     * @param builder
     * @param endpointName
     * @param testTemplateDir
     * @param testName
     * @param url
     * @param requestFile
     * @param contentType
     * @param headers
     * @return
     */
    public static TestAction processPostRequest(
            HttpActionBuilder builder,
            String endpointName,
            String testTemplateDir,
            String testName,
            String url,
            String requestFile,
            String contentType,
            Map<String, Object> headers) {

        String requestFilePath =
                MessageFormat.format("{0}/{1}/{2}", testTemplateDir, testName, requestFile);
        System.out.println("requestFilePath = " + requestFilePath);
        HttpClientRequestActionBuilder requestActionBuilder =
                builder.client(endpointName).send().post(url).messageType(MessageType.JSON);

        if (StringUtils.isNotBlank(contentType))
            requestActionBuilder.contentType(contentType);

        if (null != headers)
            requestActionBuilder = addHeaders(requestActionBuilder, headers);
        return requestActionBuilder.payload(new ClassPathResource(requestFilePath));
    }

    /**
     *
     * @param builder
     * @param endPoint
     * @param url
     * @param headers
     * @param payLoad
     * @return
     */
    public static TestAction processPutRequest(
            HttpActionBuilder builder,
            String endPoint,
            String url,
            Map<String, Object> headers,
            String payLoad) {
        HttpClientRequestActionBuilder requestActionBuilder = builder.client(endPoint).send().put(url);
        addHeaders(requestActionBuilder, headers);
        requestActionBuilder.contentType(Constant.CONTENT_TYPE_APPLICATION_JSON);
        requestActionBuilder.payload(payLoad);
        return requestActionBuilder;
    }

    /**
     *
     * @param builder
     * @param endpointName
     * @param testTemplateDir
     * @param testName
     * @param url
     * @param requestFile
     * @param contentType
     * @param headers
     * @return
     */
    public static TestAction processPatchRequest(
            HttpActionBuilder builder,
            String endpointName,
            String testTemplateDir,
            String testName,
            String url,
            String requestFile,
            String contentType,
            Map<String, Object> headers) {

        String requestFilePath =
                MessageFormat.format("{0}/{1}/{2}", testTemplateDir, testName, requestFile);
        HttpClientRequestActionBuilder requestActionBuilder =
                builder.client(endpointName).send().patch(url).messageType(MessageType.JSON);
        if (StringUtils.isNotBlank(contentType)) {
            requestActionBuilder.contentType(contentType);
        }

        if (null != headers)
            requestActionBuilder = addHeaders(requestActionBuilder, headers);

        return requestActionBuilder.payload(new ClassPathResource(requestFilePath));
    }

    /**
     *
     * @param builder
     * @param endpointName
     * @param testTemplateDir
     * @param testName
     * @param url
     * @param requestFile
     * @param contentType
     * @param headers
     * @return
     */
    public static TestAction processDeleteRequest(
            HttpActionBuilder builder,
            String endpointName,
            String testTemplateDir,
            String testName,
            String url,
            String requestFile,
            String contentType,
            Map<String, Object> headers) {

        HttpClientRequestActionBuilder requestActionBuilder =
                builder.client(endpointName).send().delete(url).messageType(MessageType.JSON);
        if (StringUtils.isNotBlank(contentType)) {
            requestActionBuilder.contentType(contentType);
        }

        if (null != headers)
            requestActionBuilder = addHeaders(requestActionBuilder, headers);

        if (StringUtils.isNotBlank(requestFile)) {
            String requestFilePath =
                    MessageFormat.format("{0}/{1}/{2}", testTemplateDir, testName, requestFile);
            return requestActionBuilder.payload(new ClassPathResource(requestFilePath));
        }
        return requestActionBuilder;
    }

    /**
     *
     * @param context
     * @param builder
     * @param endpointName
     * @param testTemplateDir
     * @param testName
     * @param requestUrl
     * @param requestFile
     * @param headers
     * @param classLoader
     * @return
     */
    public static TestAction processMultipartRequest(
            TestContext context,
            HttpActionBuilder builder,
            String endpointName,
            String testTemplateDir,
            String testName,
            String requestUrl,
            String requestFile,
            Map<String, Object> headers,
            ClassLoader classLoader) {
        String formDataFileFolderPath = MessageFormat.format("{0}/{1}", testTemplateDir, testName);
        String formDataFile =
                MessageFormat.format("{0}/{1}/{2}", testTemplateDir, testName, requestFile);
        System.out.println("formDataFile = " + formDataFile);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

        try (Scanner scanner = new Scanner(new File(classLoader.getResource(formDataFile).getFile()))) {

            while (scanner.hasNext()) {
                String[] param = scanner.nextLine().split(Constant.EQUAL_SIGN);
                if (param != null && param.length == 2) {
                    if(StringUtils.equals("fileNameValue",param[1])){
                        param[1] = context.getVariable("fileNameValue");
                        System.out.println("fileNameValue : "+context.getVariable("fileNameValue"));
                    }
                    if (param[0].equalsIgnoreCase(Constant.MULTIPART_FILE_NAME)) {
                        formData.add(
                                Constant.MULTIPART_FILE_NAME,
                                new ClassPathResource(formDataFileFolderPath + "/" + param[1]));
                    } else {
                        formData.add(param[0], TestActionUtil.getVariable(context, param[1]));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpClientRequestActionBuilder actionBuilder =
                builder
                        .client(endpointName)
                        .send()
                        .post(requestUrl)
                        .contentType(MediaType.MULTIPART_FORM_DATA);


        if (null != headers)
            actionBuilder = addHeaders(actionBuilder, headers);

        return actionBuilder.payload(formData);
    }

    /**
     *
     * @param builder
     * @param endpointName
     * @param testTemplateDir
     * @param testName
     * @param responseCode
     * @param responseFile
     * @param validationParams
     * @return
     */
    public static TestAction getResponse(
            HttpActionBuilder builder,
            String endpointName,
            String testTemplateDir,
            String testName,
            HttpStatus responseCode,
            String responseFile,
            Map<String, Object> validationParams) {

        if (MapUtils.isNotEmpty(validationParams)) {
            String path = objectTypes.stream().filter(x->testTemplateDir.toLowerCase().contains(x)).collect(Collectors.toList()).get(0);
            System.out.println("path : "+path);
            HttpClientActionBuilder.HttpClientReceiveActionBuilder response = builder.client(endpointName).receive();
            return response
                    .response(responseCode)
                    .validationCallback(
                            new JsonMappingValidationCallback<Response>(Response.class, objectMapper) {
                                @Override
                                public void validate(
                                        Response response, Map<String, Object> headers, TestContext context) {
                                    System.out.println("Going With Dynamic Validation. | API Result : "+response.getResult());
                                    System.out.println("Validation Params : "+validationParams);
                                    Assert.assertEquals(response.getResponseCode().code(), responseCode.value());
                                    Map<String, Object> result = (Map<String, Object>) response.getResult().get(path);
                                    for(String key : validationParams.keySet()){
                                            Assert.assertTrue(result.containsKey(key));
                                            System.out.println("Key Matched. Key = "+key+" | Value from Response = "+result.get(key));

                                            if (null != validationParams.get(key)) {
                                                System.out.println("Checking for Exact Value Match!");
                                                Assert.assertEquals(validationParams.get(key), result.get(key));
                                            } else {
                                                System.out.println("Checking for Not Null!");
                                                Assert.assertNotNull(result.get(key));
                                            }

                                    }
                                }
                            });
        }

        if (MapUtils.isEmpty(validationParams) && StringUtils.isBlank(responseFile)) {
            System.out.println("Dynamic/Static Validation Skipped. Only Response Code Will be Validated.");
            return getResponse(builder, endpointName, testName, responseCode);
        }
        System.out.println("Going With Static Validation Against Response File!");
        String responseFilePath =
                MessageFormat.format("{0}/{1}/{2}", testTemplateDir, testName, responseFile);

        return builder
                .client(endpointName)
                .receive()
                .response(responseCode)
                .validator("defaultJsonMessageValidator")
                .messageType(MessageType.JSON)
                .payload(new ClassPathResource(responseFilePath));
    }

    /**
     *
     * @param builder
     * @param endpointName
     * @param testName
     * @param responseCode
     * @return
     */
    public static TestAction getResponse(
            HttpActionBuilder builder, String endpointName, String testName, HttpStatus responseCode) {
        return builder
                .client(endpointName)
                .receive()
                .response(responseCode)
                .validator("defaultJsonMessageValidator");
    }

    /**
     *
     * @param testContext
     * @param builder
     * @param endpointName
     * @param responseCode
     * @param extractFieldPath
     * @param extractVariable
     * @return
     */
    public static TestAction getExtractFromResponseTestAction(
            TestContext testContext,
            HttpActionBuilder builder,
            String endpointName,
            HttpStatus responseCode,
            String extractFieldPath,
            String extractVariable) {
        ObjectMapper mapper = new ObjectMapper();
        return builder
                .client(endpointName)
                .receive()
                .response(responseCode)
                .validator("defaultJsonMessageValidator")
                .messageType(MessageType.JSON)
                .extractFromPayload(extractFieldPath, extractVariable)
                .validationCallback(
                        new JsonMappingValidationCallback<Map>(Map.class, mapper) {
                            @Override
                            public void validate(Map response, Map<String, Object> headers, TestContext context) {
                                Object extractValue = context.getVariables().get(extractVariable);
                                testContext.getVariables().put(extractVariable, extractValue);
                                System.out.println("extractVariable = " + extractValue);
                            }
                        });
    }

    /**
     *
     * @param actionBuilder
     * @param headers
     * @return
     */
    private static HttpClientRequestActionBuilder addHeaders(
            HttpClientRequestActionBuilder actionBuilder, Map<String, Object> headers) {
        if (headers != null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                actionBuilder = actionBuilder.header(entry.getKey(), entry.getValue());
            }
        }
        return actionBuilder;
    }

    /**
     *
     * @param testContext
     * @param variableName
     * @return
     */
    private static String getVariable(TestContext testContext, String variableName) {
        String value;
        try {
            value = testContext.getVariable(variableName);
        } catch (CitrusRuntimeException exception) {
            value = variableName;
        }
        return value;
    }

    /**
     *
     * @param builder
     * @param endPointName
     * @param url
     * @param contentType
     * @param payload
     * @param headers
     * @return
     */
    public static TestAction getPostRequestTestAction(
            HttpActionBuilder builder,
            String endPointName,
            String url,
            String contentType,
            String payload,
            Map<String, Object> headers) {
        HttpClientRequestActionBuilder requestActionBuilder = builder.client(endPointName).send().post(url).messageType(MessageType.JSON);
        addHeaders(requestActionBuilder, headers);
        requestActionBuilder.contentType(contentType);
        requestActionBuilder.payload(payload);
        return requestActionBuilder;
    }

    /**
     *
     * @param builder
     * @param endPointName
     * @param url
     * @param contentType
     * @param payload
     * @param headers
     * @return
     */
    public static TestAction getPutRequestTestAction(
            HttpActionBuilder builder,
            String endPointName,
            String url,
            String contentType,
            String payload,
            Map<String, Object> headers) {
        HttpClientRequestActionBuilder requestActionBuilder = builder.client(endPointName).send().put(url).messageType(MessageType.JSON);
        addHeaders(requestActionBuilder, headers);
        requestActionBuilder.contentType(contentType);
        requestActionBuilder.payload(payload);
        return requestActionBuilder;
    }

    /**
     *
     * @param builder
     * @param endPointName
     * @param url
     * @param contentType
     * @param payload
     * @param headers
     * @return
     */
    public static TestAction getPatchRequestTestAction(
            HttpActionBuilder builder,
            String endPointName,
            String url,
            String contentType,
            String payload,
            Map<String, Object> headers) {
        HttpClientRequestActionBuilder requestActionBuilder = builder.client(endPointName).send().patch(url).messageType(MessageType.JSON);
        addHeaders(requestActionBuilder, headers);
        requestActionBuilder.contentType(contentType);
        requestActionBuilder.payload(payload);
        return requestActionBuilder;
    }

    /**
     *
     * @param builder
     * @param endPointName
     * @param url
     * @param contentType
     * @param payload
     * @param headers
     * @return
     */
    public static TestAction getDeleteRequestTestAction(
            HttpActionBuilder builder,
            String endPointName,
            String url,
            String contentType,
            String payload,
            Map<String, Object> headers) {
        HttpClientRequestActionBuilder requestActionBuilder = builder.client(endPointName).send().delete(url).messageType(MessageType.JSON);
        addHeaders(requestActionBuilder, headers);
        requestActionBuilder.contentType(contentType);
        requestActionBuilder.payload(payload);
        return requestActionBuilder;
    }

}
