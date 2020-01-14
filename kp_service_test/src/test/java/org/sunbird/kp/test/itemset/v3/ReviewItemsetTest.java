package org.sunbird.kp.test.itemset.v3;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.CitrusParameters;
import org.springframework.http.HttpStatus;
import org.sunbird.kp.test.common.APIUrl;
import org.sunbird.kp.test.common.BaseCitrusTestRunner;
import org.sunbird.kp.test.common.Constant;
import org.sunbird.kp.test.util.ItemsetUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;

public class ReviewItemsetTest extends BaseCitrusTestRunner {
    private static final String TEMPLATE_DIR = "templates/itemset/v3/review";
    private String identifier;

    @Test(dataProvider = "reviewItemsetWithValidRequest")
    @CitrusParameters({"testName","payloadName"})
    @CitrusTest
    public void testReviewItemsetWithValidRequest(String testName, String payloadName) {
        getAuthToken(this, Constant.CREATOR);
        identifier = (String) ItemsetUtil.createItemset(this, this.testContext, null, payloadName, null).get("itemset_id");
        this.variable("itemsetId", identifier);
        performPostTest(this, TEMPLATE_DIR, testName, APIUrl.REVIEW_ITEMSET + identifier, null,
                REQUEST_JSON, MediaType.APPLICATION_JSON, HttpStatus.OK, null, RESPONSE_JSON);
        //Read The Itemset And Validate
        performGetTest(this, TEMPLATE_DIR, testName, APIUrl.READ_ITEMSET + identifier, null,
                HttpStatus.OK, null, VALIDATE_JSON);
    }

    @DataProvider(name = "reviewItemsetWithValidRequest")
    public Object[][] reviewItemsetWithValidRequest() {
        return new Object[][]{
                new Object[]{
                        ItemsetV3Scenario.TEST_REVIEW_ITEMSET_WITH_VALID_REQUEST, "createItemsetExpect200"
                }
        };
    }
}
