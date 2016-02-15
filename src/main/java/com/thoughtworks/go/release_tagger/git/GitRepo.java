package com.thoughtworks.go.release_tagger.git;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import org.apache.cxf.jaxrs.client.WebClient;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis;

public class GitRepo {

    private final String GIT_API_URI = "https://api.github.com/repos/";
    private String authToken;
    private String gitRepoUri;

    public GitRepo(String repoUserName, String repoName, String authToken) {
        this.authToken = authToken;
        gitRepoUri = GIT_API_URI + repoUserName + "/" + repoName;
    }

    public void createTag(String userName, String email, String commitHash, String tagName, String message) {
        JsonObject requestBody = tagBody(userName, email, commitHash, tagName, message);
        JSONObject tagCreateResponse = post(gitRepoUri + "/git/tags?access_token=" + this.authToken, requestBody);
        post(gitRepoUri + "/git/refs?access_token=" + authToken, Json.createObjectBuilder()
                .add("ref", "refs/tags/" + requestBody.getString("tag"))
                .add("sha", (String) tagCreateResponse.get("sha")).build());
    }

    private JsonObject tagBody(String userName, String email, String commitHash, String tagName, String message) {
        JsonObject tagger = Json.createObjectBuilder()
                .add("name", userName)
                .add("email", email)
                .add("date", dateTimeNoMillis().print(DateTime.now())).build();
        return Json.createObjectBuilder()
                .add("tag", tagName)
                .add("message", message)
                .add("object", commitHash)
                .add("type", "commit")
                .add("tagger", tagger)
                .build();
    }

    private JSONObject post(String url, JsonObject requestBody){
        Response response = WebClient.create(url)
                .accept(APPLICATION_JSON)
                .type(APPLICATION_JSON)
                .post(requestBody.toString());
        if(response.getStatus() != 201)
            throw new RuntimeException(response.getStatus() + " - Git Api Error while posting to " + url + " with " + requestBody.toString() + " . Got " + response.toString() );
        return (JSONObject) JsonPath.parse(response.readEntity(String.class)).json();
    }
}
