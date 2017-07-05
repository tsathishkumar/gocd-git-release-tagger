package com.thoughtworks.go.release_tagger;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.release_tagger.executors.ReleaseTaggerExecutor;
import com.thoughtworks.go.release_tagger.views.ReleaseTaggerView;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Extension
public class ReleaseTaggerTask implements GoPlugin {
    public static final String ADDITIONAL_OPTIONS = "AdditionalOptions";

    static Logger logger = Logger.getLoggerFor(ReleaseTaggerTask.class);

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        try {
            if ("configuration".equals(request.requestName())) {
                return handleGetConfigRequest();
            } else if ("validate".equals(request.requestName())) {
                return handleValidation(request);
            } else if ("execute".equals(request.requestName())) {
                return handleTaskExecution(request);
            } else if ("view".equals(request.requestName())) {
                return handleTaskView();
            }
        } catch (IOException e) {
            logError(e, "error when serializing");
            throw new UnhandledRequestTypeException(request.requestName());
        }
        throw new UnhandledRequestTypeException(request.requestName());
    }

    private GoPluginApiResponse handleTaskExecution(GoPluginApiRequest request) throws IOException {
        int responseCode = DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;
        Map executionRequest = (Map) new JacksonFactory().fromString(request.requestBody(), Object.class);
        Map context = (Map) executionRequest.get("context");

        try {
            new ReleaseTaggerExecutor(context).execute();
        } catch (Exception e) {
            logError(e, "Error when executing");
        }
        HashMap body = new HashMap();
        body.put("success", true);
        body.put("message", "Artifacts downloaded successfully");
        return createResponse(responseCode, body);
    }

    private GoPluginApiResponse handleValidation(GoPluginApiRequest request) throws IOException {
        int responseCode = DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;
        HashMap body = new HashMap();
        body.put("errors", new HashMap());
        return createResponse(responseCode, body);
    }

    private GoPluginApiResponse createResponse(int responseCode, Map body) throws IOException {
        final DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(responseCode);
        String bodyString = new JacksonFactory().toString(body);
        response.setResponseBody(bodyString);
        return response;
    }

    private GoPluginApiResponse handleGetConfigRequest() throws IOException {
        HashMap config = new HashMap();

        HashMap additionalOptions = new HashMap();
        additionalOptions.put("display-order", "0");
        additionalOptions.put("display-name", "Additional Options");
        additionalOptions.put("required", false);
        config.put(ADDITIONAL_OPTIONS, additionalOptions);

        return createResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, config);
    }

    private GoPluginApiResponse handleTaskView() throws IOException {
        int responseCode = DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE;
        Map view = new HashMap();
        ReleaseTaggerView oldView = new ReleaseTaggerView();
        view.put("displayValue", oldView.displayValue());
        try {
            view.put("template", oldView.template());
        } catch (Exception e) {
            responseCode = DefaultGoPluginApiResponse.INTERNAL_ERROR;
            String errorMessage = "Failed to find template: " + e.getMessage();
            view.put("exception", errorMessage);
            logError(e, errorMessage);
        }
        return createResponse(responseCode, view);
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("task", Arrays.asList("1.0"));
    }

    public static void logError(Exception e, String errorMessage) {
        logger.error(errorMessage, e);
    }

    public static void logInfo(String message) {
        logger.info(message);
    }
}
