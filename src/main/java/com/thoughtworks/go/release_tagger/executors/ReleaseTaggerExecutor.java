package com.thoughtworks.go.release_tagger.executors;

import com.thoughtworks.go.release_tagger.ReleaseTaggerTask;
import com.thoughtworks.go.release_tagger.core.GitReleaseTagger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ReleaseTaggerExecutor {

    private static final String GO_SERVER_URL = "GO_SERVER_URL";
    private static final String GO_PIPELINE_NAME = "GO_PIPELINE_NAME";
    private static final String GO_PIPELINE_COUNTER = "GO_PIPELINE_COUNTER";
    private static final String GO_USER_NAME = "GO_USER_NAME";
    private static final String GO_USER_EMAIL = "GO_USER_EMAIL";
    private static final String GO_PASSWORD = "GO_PASSWORD";
    private static final String GIT_AUTH_TOKEN = "GIT_AUTH_TOKEN";

    private Map context;

    public ReleaseTaggerExecutor(Map context) {
        this.context = context;
    }

    public void execute() throws Exception {
            String serverUrl = getEnvironmentVariable(GO_SERVER_URL);
            String pipeline = getEnvironmentVariable(GO_PIPELINE_NAME);
            String pipelineCounter = getEnvironmentVariable(GO_PIPELINE_COUNTER);
            String userName = getEnvironmentVariable(GO_USER_NAME);
            String email = getEnvironmentVariable(GO_USER_EMAIL);
            String password = getEnvironmentVariable(GO_PASSWORD);
            String authToken = getEnvironmentVariable(GIT_AUTH_TOKEN);

            String pipelineValueStreamMapUrl = serverUrl + "pipelines/value_stream_map/" + pipeline + "/" + pipelineCounter + ".json";
            GitReleaseTagger gitReleaseTagger = new GitReleaseTagger();
            String tagName = gitReleaseTagger.tagAllDependentRepos(pipelineValueStreamMapUrl, pipelineCounter, userName, password, authToken, email);

            ReleaseTaggerTask.logInfo("All dependent repositories are tagged with tagName: "+tagName);
    }

    private String getEnvironmentVariable(String key) throws Exception {
        Map<String, String> environmentMap = getEnvironmentFromTaskExecutionContext();
        String valueForGivenKey = environmentMap.get(key);
        if (valueForGivenKey == null || valueForGivenKey.isEmpty()) {
            throw new Exception("Environment variable " + key + " has not value set");
        }

        return valueForGivenKey;
    }

    private Map<String, String> getEnvironmentFromTaskExecutionContext() throws MalformedURLException {
        Map<String, String> environmentMap = (Map<String, String>) getTaskExecutionContext().get("environmentVariables");
        Map<String, String> mutableEnvironmentMap = new HashMap<String, String>();
        for (String key : environmentMap.keySet()) {
            mutableEnvironmentMap.put(key, environmentMap.get(key));
        }
        URL goServerURL = new URL(environmentMap.get(GO_SERVER_URL));
        mutableEnvironmentMap.put(GO_SERVER_URL, "http://" + goServerURL.getHost() + ":8153/go/");
        return mutableEnvironmentMap;
    }

    private Map getTaskExecutionContext() {
        return context;
    }

}
