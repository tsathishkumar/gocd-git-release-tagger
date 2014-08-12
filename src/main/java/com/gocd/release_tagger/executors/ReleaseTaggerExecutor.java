package com.gocd.release_tagger.executors;

import com.gocd.release_tagger.core.GitReleaseTagger;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;

import java.util.Map;

public class ReleaseTaggerExecutor implements TaskExecutor {

    @Override
    public ExecutionResult execute(TaskConfig taskConfig, TaskExecutionContext taskExecutionContext) {
        try {
            Map<String, String> environmentVariables = taskExecutionContext.environment().asMap();
            String serverUrl = environmentVariables.get("GO_SERVER_URL");
            String pipeline = environmentVariables.get("GO_PIPELINE_NAME");
            String pipelineCounter = environmentVariables.get("GO_PIPELINE_COUNTER");
            String userName = environmentVariables.get("GO_USER_NAME");
            String email = environmentVariables.get("GO_USER_EMAIL");
            String password = environmentVariables.get("GO_PASSWORD");
            String authToken = environmentVariables.get("GO_AUTH_TOKEN");

            String pipelineValueStreamMapUrl = serverUrl + "pipelines/value_stream_map/" + pipeline +"/" +pipelineCounter + ".json";

            GitReleaseTagger gitReleaseTagger = new GitReleaseTagger();
            String tag = gitReleaseTagger.tagAllDependentRepos(pipelineValueStreamMapUrl, pipelineCounter, userName, password, authToken, email);


            return ExecutionResult.success("All dependent repo's are tagged with name: " + tag);
        } catch (Exception e) {
            taskExecutionContext.console().printLine(e.getMessage());
            taskExecutionContext.console().printLine(stackTraceToString(e));
            return ExecutionResult.failure("Release tagging failed", e);
        }
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
