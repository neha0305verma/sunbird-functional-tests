package org.sunbird.kp.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.sunbird.kp.test.common.AppConfig;
import org.sunbird.kp.test.common.Response;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

public class TestSetupUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();

    /**
     * Method creates, populates and modifies the assertions for validate.json.
     * TODO: MUST be extended for response.json
     * @param dirIdMap (Mapping of directoryNames and ContentIds)
     * @param validationFileName (Should be validate.json or response.json)
     */
    public static void createDirectoriesForTestCases(Map<String, String> dirIdMap, String validationFileName, String templateDir)  {
        Set<String> directoryNames = dirIdMap.keySet();
        String mainDir = System.getProperty("user.dir") + "/src/test/resources/" + templateDir;
        directoryNames.forEach(dir -> {
            File file = new File(mainDir +"/"+ dir);
            file.mkdir();
            if (file.isDirectory()) {
                File responseFile = new File(file.getPath() + "/" + "response.json");
                File requestFile = new File(file.getPath() + "/" + "request.json");
                File validateFile = new File(file.getPath() + "/" + "validate.json");
                try {
                    if (!responseFile.exists())
                        responseFile.createNewFile();
                    if (!requestFile.exists())
                        requestFile.createNewFile();
                    if (!validateFile.exists())
                        validateFile.createNewFile();
                    populateDataIntoValidateFiles(mainDir, dir, validationFileName, dirIdMap.get(dir),templateDir);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("No files are created");
                }
            }
        });
    }

    private static void populateDataIntoValidateFiles(String mainDir, String directoryPath, String fileName, String contentId, String templateDir) {
        File file = new File(mainDir + "/" + directoryPath);
        if (file.isDirectory()) {
            try {
                FileWriter fileWriter = new FileWriter(file.getPath() + "/" + fileName);
                String[] template = templateDir.split("/",4);
                String baseUri = null;
                String readUrl = null;
                switch(template[1]){
                    case "content" : {
                        baseUri = "kp_base_uri";
                        readUrl = "/content/v3/read/";
                    }
                    case "itemset" : {
                        baseUri = "kp_assessment_service_base_uri";
                        readUrl = "/itemset/v3/read/";
                    }
                }
                HttpResponse<String> jsonNode = Unirest.get(AppConfig.config.getString(baseUri) + readUrl + contentId + "").asString();
                Response response = objectMapper.readValue(jsonNode.getBody(), Response.class);
                fileWriter.write(formGeneralAssertions(response));
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String formGeneralAssertions(Response response) {
        String oldString = "";
        try {
            oldString = objectWriter.writeValueAsString(response);
            String updatedString = oldString
                    .replaceAll("\\d{4}-\\d{2}-\\d{2}T[a-zA-Z0-9:.+]+", "@matchesDatePattern('yyyy-MM-dd')@" )
                    .replaceAll("\"resmsgid\"\\W?:\\W?\"[0-9a-zA-Z-]+\"", "\"resmsgid\" : \"@ignore@\"")
                    .replaceAll("\"previewUrl\"\\W?:\\W\"[a-zA-Z://._0-9-]+\"","\"previewUrl\" : \"@ignore@\"")
                    .replaceAll("\"downloadUrl\"\\W?:\\W\"[a-zA-Z://._0-9-]+\"","\"downloadUrl\" : \"@ignore@\"")
                    .replaceAll("\"ecarUrl\"\\W?:\\W\"[a-zA-Z://._0-9-]+\"","\"ecarUrl\" : \"@ignore@\"")
                    .replaceAll("\"streamingUrl\"\\W?:\\W\"[a-zA-Z://._0-9-]+\"","\"streamingUrl\" :\"@ignore@\"")
                    .replaceAll("\"artifactUrl\"\\W?:\\W\"[a-zA-Z://._0-9-]+\"","\"artifactUrl\" : \"@ignore@\"")
                    .replaceAll("\"s3Key\"\\W?:\\W\"[a-zA-Z:/._0-9-]+\"","\"s3Key\" : \"@ignore@\"")
                    .replaceAll("\"appId\"\\W?:\\W?\"[a-zA-Z.-]+\"","\"appId\" : \"@ignore@\"")
                    .replaceAll("\"consumerId\"\\W?:\\W?\"[a-zA-Z0-9-]+\"","\"consumerId\" : \"@ignore@\"")
                    .replaceAll("KP_FT_\\d+","\\${contentIdVal}")
                    .replaceAll("\\s\\d+\\.\\d+","\"@isNumber()@\"")
                    .replaceAll("\\s\\d", "\"@isNumber()@\"")
                    .replaceAll("\"versionKey\"\\W?:\\W\"\\d+\"","\"versionKey\" : \"@ignore@\"");
            return updatedString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oldString;
    }

}
