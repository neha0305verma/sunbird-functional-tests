package org.sunbird.kp.test.itemset.v3;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.CitrusParameters;
import org.springframework.http.HttpStatus;
import org.sunbird.kp.test.common.APIUrl;
import org.sunbird.kp.test.common.BaseCitrusTestRunner;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;

public class CreateItemsetTest extends BaseCitrusTestRunner {

    private static final String TEMPLATE_DIR = "templates/itemset/v3/create";
    private String identifier;

    @Test(dataProvider = "createItemsetWithValidRequest")
    @CitrusParameters("testName")
    @CitrusTest
    public void testCreateItemsetWithValidRequest(String testName) {
        identifier = "KP_FT_" + generateRandomDigits(9);
        this.variable("itemsetId", identifier);
        performPostTest(this, TEMPLATE_DIR, testName, APIUrl.CREATE_ITEMSET, null,
                REQUEST_JSON, MediaType.APPLICATION_JSON, HttpStatus.OK, null, RESPONSE_JSON);
        //Read The Itemset And Validate
        performGetTest(this, TEMPLATE_DIR, testName, APIUrl.READ_ITEMSET + identifier, null,
                HttpStatus.OK, null, VALIDATE_JSON);
    }

    @DataProvider(name = "createItemsetWithValidRequest")
    public Object[][] createItemsetWithValidRequest() {
        return new Object[][]{
                new Object[]{
                        ItemsetV3Scenario.TEST_CREATE_ITEMSET_WITH_VALID_REQUEST
                }
        };
    }
}
