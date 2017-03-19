package com.thoughtworks.go.release_tagger.core;

import com.jayway.jsonpath.JsonPath;
import com.thoughtworks.go.release_tagger.git.GitRepo;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.cxf.jaxrs.client.WebClient;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class GitReleaseTagger {
    public static final String YYYY_MM_DD_KK_MM_SS = "yyyy-MM-dd_kk-mm-ss";
    public static final Pattern REGEX = Pattern.compile("^.*github.com[:/](?<user>[^/]+)/(?<repo>[^/]+)$");

    public String tagAllDependentRepos(String pipelineValueStreamMapUrl, String pipelineCounter,
                                       String username, String password, String authToken, String email) throws Exception {
        String currentDateTime = DateTime.now().toString(YYYY_MM_DD_KK_MM_SS);
        String tag = format("v%s_%s", pipelineCounter, currentDateTime);

        JSONArray nodes = fetchDependentRepos(pipelineValueStreamMapUrl, username, password);

        Map<String, Map<String, Integer>> processedRepos = new HashMap<String, Map<String, Integer>>();

        for (Object node : nodes) {
            String gitUrl = (String) ((JSONObject) node).get("name");

            String repoName = getRepoFromGitUrl(gitUrl);
            String repoUserName = getUserFromGitUrl(gitUrl);

            String commitHash = (String)((JSONObject)((JSONArray) ((JSONObject) ((JSONArray) ((JSONObject) node).get("material_revisions")).get(0)).get("modifications")).get(0)).get("revision");
            Map<String, Integer> repoShaCount = processedRepos.getOrDefault(repoName, new HashMap<String, Integer>());
            repoShaCount.put(commitHash, repoShaCount.getOrDefault(commitHash, 0) + 1);
            processedRepos.put(repoName, repoShaCount);
            if (repoShaCount.getOrDefault(commitHash, 0) > 1) {
                continue;
            }

            GitRepo gitRepo = new GitRepo(repoUserName, repoName, authToken);
            gitRepo.createTag(username, email, commitHash, tag, "Released on " + currentDateTime);
        }
        return tag;
    }

    private JSONArray fetchDependentRepos(String pipelineValueStreamMapUrl, String username, String password) {
        WebClient webClient = WebClient.create(pipelineValueStreamMapUrl, username, password, null);
        String response = webClient.get(String.class);
        return JsonPath.read(response, "$.levels[*].nodes[?(@.node_type == 'GIT')]");
    }

    public String getRepoFromGitUrl(String gitUrl) throws Exception {
        return extractFromString(gitUrl, "repo").replace(".git","");
    }

    public String getUserFromGitUrl(String gitUrl) throws Exception {
        return extractFromString(gitUrl, "user");
    }

    private String extractFromString(String gitUrl, String groupName) throws Exception {
        Matcher matcher = REGEX.matcher(gitUrl);
        if(matcher.find()) {
            return matcher.group(groupName);
        }
        throw new Exception("URL did not match expected pattern");
    }
}
