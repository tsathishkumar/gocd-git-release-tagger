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

    @Override
    public ExecutionResult execute(TaskConfig taskConfig, TaskExecutionContext taskExecutionContext) {

        taskExecutionContext.console().printLine("Running Git Release Tagger");
        try {
            Map<String, String> environmentVariables = environmentVariablesMap(taskExecutionContext);
            String serverUrl = environmentVariables.get("GO_SERVER_URL");
            String pipeline = environmentVariables.get("GO_PIPELINE_NAME");
            String pipelineCounter = environmentVariables.get("GO_PIPELINE_COUNTER");
            String userName = environmentVariables.get("GO_USER_NAME");
            String email = environmentVariables.get("GO_USER_EMAIL");
            String password = environmentVariables.get("GO_PASSWORD");
            String authToken = environmentVariables.get("GO_AUTH_TOKEN");

            String pipelineValueStreamMapUrl = serverUrl + "pipelines/value_stream_map/" + pipeline +"/" +pipelineCounter + ".json";
            GitReleaseTagger gitReleaseTagger = new GitReleaseTagger();
            taskExecutionContext.console().printLine("pipelineValueStreamMapUrl:"+pipelineValueStreamMapUrl+" pipelineCounter:"
                            +pipelineCounter+" userName:"+userName+" password:"+password+" authToken:"+authToken+" email:"+email);
            String tag = gitReleaseTagger.tagAllDependentRepos(pipelineValueStreamMapUrl, pipelineCounter, userName, password, authToken, email, taskExecutionContext);

            return ExecutionResult.success("All dependent repo's are tagged with name: " + tag);
        } catch (Throwable e) {
            taskExecutionContext.console().printLine(stackTraceToString(e));
            return ExecutionResult.failure("Release tagging failed", new Exception(e));
        }
    }

    private Map<String, String> environmentVariablesMap(TaskExecutionContext taskExecutionContext) throws MalformedURLException {
        Map<String, String> environmentMap = taskExecutionContext.environment().asMap();
        Map<String, String> mutableEnvironmentMap = new HashMap<String, String>();
        for(String key : environmentMap.keySet()) {
            mutableEnvironmentMap.put(key, environmentMap.get(key));
        }
        URL goServerURL = new URL(environmentMap.get("GO_SERVER_URL"));
        mutableEnvironmentMap.put("GO_SERVER_URL","http://"+goServerURL.getHost()+":8153/go/");
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
