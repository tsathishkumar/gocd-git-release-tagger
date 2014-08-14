package com.thoughtworks.go.release_tagger.core;

import com.jayway.jsonpath.JsonPath;
import com.thoughtworks.go.release_tagger.git.GitRepo;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.cxf.jaxrs.client.WebClient;
import org.joda.time.DateTime;

import java.io.IOException;

import static java.lang.String.format;

public class GitReleaseTagger {
    public static final String YYYY_MM_DD_KK_MM_SS = "yyyy-MM-dd_kk-mm-ss";

    public String tagAllDependentRepos(String pipelineValueStreamMapUrl, String pipelineCounter,
                                       String username, String password, String authToken, String email) throws IOException {
        String currentDateTime = DateTime.now().toString(YYYY_MM_DD_KK_MM_SS);
        String tag = format("v%s_%s", pipelineCounter, currentDateTime);

        WebClient webClient = WebClient.create(pipelineValueStreamMapUrl, username, password,null);
        String response = webClient.get(String.class);
        JSONArray nodes = JsonPath.read(response, "$.levels[*].nodes[?(@.node_type == 'GIT')]");

        for(Object node : nodes){
            String gitUrl = (String) ((JSONObject) node).get("name");
            String repoName = repoName(gitUrl);
            String repoUserName = userName(gitUrl);

            String commitHash = (String) ((JSONObject) ((JSONArray) ((JSONObject) node).get("instances")).get(0)).get("revision");

            GitRepo gitRepo = new GitRepo(repoUserName, repoName, authToken);
            gitRepo.createTag(username, email, commitHash, tag, "Released on " + currentDateTime);
        }
        return tag;
    }

    private String userName(String gitUrl) {
        return gitUrl.split(":")[1].split("/")[0];
    }

    private String repoName(String gitUrl) {
        return gitUrl.split("/")[1].split("\\.")[0];
    }

   }
