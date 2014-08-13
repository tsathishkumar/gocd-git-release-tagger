package com.thoughtworks.go.release_tagger.views;

import com.thoughtworks.go.plugin.api.task.TaskView;
import org.apache.commons.io.IOUtils;

public class ReleaseTaggerView implements TaskView {
    @Override
    public String displayValue() {
        return "Git Release Tagger";
    }

    @Override
    public String template() {
        try {
            return IOUtils.toString(getClass().getResourceAsStream("/views/release-tagger.template.html"), "UTF-8");
        } catch (Exception e) {
            return "Failed to find template: " + e.getMessage();
        }
    }
}
