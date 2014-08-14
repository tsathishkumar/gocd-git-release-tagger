package com.thoughtworks.go.release_tagger.executors;

import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;
import com.thoughtworks.go.release_tagger.core.GitReleaseTagger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ReleaseTaggerExecutor implements TaskExecutor {

    public static final String GO_SERVER_URL = "GO_SERVER_URL";
    public static final String GO_PIPELINE_NAME = "GO_PIPELINE_NAME";
    public static final String GO_PIPELINE_COUNTER = "GO_PIPELINE_COUNTER";
    public static final String GO_USER_NAME = "GO_USER_NAME";
    public static final String GO_USER_EMAIL = "GO_USER_EMAIL";
    public static final String GO_PASSWORD = "GO_PASSWORD";
    public static final String GO_AUTH_TOKEN = "GO_AUTH_TOKEN";

    @Override
    public ExecutionResult execute(TaskConfig taskConfig, TaskExecutionContext taskExecutionContext) {

        try {
            Map<String, String> environmentVariables = environmentVariablesMap(taskExecutionContext);
            String serverUrl = environmentVariables.get(GO_SERVER_URL);
            String pipeline = environmentVariables.get(GO_PIPELINE_NAME);
            String pipelineCounter = environmentVariables.get(GO_PIPELINE_COUNTER);
            String userName = environmentVariables.get(GO_USER_NAME);
            String email = environmentVariables.get(GO_USER_EMAIL);
            String password = environmentVariables.get(GO_PASSWORD);
            String authToken = environmentVariables.get(GO_AUTH_TOKEN);

            String pipelineValueStreamMapUrl = serverUrl + "pipelines/value_stream_map/" + pipeline +"/" +pipelineCounter + ".json";
            GitReleaseTagger gitReleaseTagger = new GitReleaseTagger();
            String tagName = gitReleaseTagger.tagAllDependentRepos(pipelineValueStreamMapUrl, pipelineCounter, userName, password, authToken, email);

            taskExecutionContext.console().printLine("[Git Release Tagger] All dependent git repositories are tagged with: " + tagName);

            return ExecutionResult.success("[Git Release Tagger] All dependent git repositories are tagged with: " + tagName);
        } catch (Exception e) {
            taskExecutionContext.console().printLine(stackTraceToString(e));
            return ExecutionResult.failure("[Git Release Tagger] Release tagging failed", e);
        }
    }

    private Map<String, String> environmentVariablesMap(TaskExecutionContext taskExecutionContext) throws MalformedURLException {
        Map<String, String> environmentMap = taskExecutionContext.environment().asMap();
        Map<String, String> mutableEnvironmentMap = new HashMap<String, String>();
        for(String key : environmentMap.keySet()) {
            mutableEnvironmentMap.put(key, environmentMap.get(key));
        }
        URL goServerURL = new URL(environmentMap.get(GO_SERVER_URL));
        mutableEnvironmentMap.put(GO_SERVER_URL,"http://"+goServerURL.getHost()+":8153/go/");
        return mutableEnvironmentMap;
    }

    public String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
