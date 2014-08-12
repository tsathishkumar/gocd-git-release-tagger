package com.gocd.release_tagger.core;

import com.jayway.jsonpath.JsonPath;
import com.jcabi.github.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.cxf.jaxrs.client.WebClient;
import org.joda.time.DateTime;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;

import static java.lang.String.format;

public class GitReleaseTagger {

    public String tagAllDependentRepos(String pipelineValueStreamMapUrl, String pipelineCounter, String username, String password, String authToken, String email) throws IOException {
        String currentDateTime = DateTime.now().toString("yyyy-MM-dd_k-m-s");
        String tag = format("v%s_%s", pipelineCounter, currentDateTime);

        WebClient webClient = WebClient.create(pipelineValueStreamMapUrl, username, password,null);
        String response = webClient.get(String.class);
        JSONArray nodes = JsonPath.read(response, "$.levels[*].nodes[?(@.node_type == 'GIT')]");
        Github github = new RtGithub(authToken);

        for(Object node : nodes){
            String gitUrl = (String) ((JSONObject) node).get("name");
            String repoName = repoName(gitUrl);
            String repoUserName = userName(gitUrl);

            String commitHash = (String) ((JSONObject) ((JSONArray) ((JSONObject) node).get("instances")).get(0)).get("revision");
            Repo repo = github.repos().get(new Coordinates.Simple(repoUserName+"/"+repoName));

            JsonObject requestBody = tagBody(username, email, currentDateTime, commitHash, tag);

            repo.git().tags().create(requestBody);
        }
        return tag;
    }

    private JsonObject tagBody(String userName, String email, String currentDateTime, String commitHash, String tag) {
        JsonObject tagger = Json.createObjectBuilder()
                .add("name", userName)
                .add("email", email)
                .add("date", DateTime.now().toString()).build();
        return Json.createObjectBuilder()
                .add("tag", tag)
                .add("message", format("Release on %s", currentDateTime))
                .add("object", commitHash)
                .add("type", "commit")
                .add("tagger", tagger)
                .build();
    }

    private String userName(String gitUrl) {
        return gitUrl.split(":")[1].split("/")[0];
    }

    private String repoName(String gitUrl) {
        return gitUrl.split("/")[1].split("\\.")[0];
    }

}
