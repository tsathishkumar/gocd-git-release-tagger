package com.thoughtworks.go.release_tagger.views;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class ReleaseTaggerView {
    public String displayValue() {
        return "Git Release Tagger";
    }

    public String template() throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("/views/release-tagger.template.html"), "UTF-8");
    }
}
