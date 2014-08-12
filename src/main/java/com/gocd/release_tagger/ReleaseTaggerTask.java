package com.gocd.release_tagger;

import com.gocd.release_tagger.executors.ReleaseTaggerExecutor;
import com.gocd.release_tagger.views.ReleaseTaggerView;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.Task;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;
import com.thoughtworks.go.plugin.api.task.TaskView;

@Extension
public class ReleaseTaggerTask implements Task {
    @Override
    public TaskConfig config() {
        TaskConfig config = new TaskConfig();
        return config;
    }

    @Override
    public TaskExecutor executor() {
        return new ReleaseTaggerExecutor();
    }


    @Override
    public TaskView view() {
        return new ReleaseTaggerView();
    }

    @Override
    public ValidationResult validate(TaskConfig taskConfig) {
        return new ValidationResult();
    }
}
