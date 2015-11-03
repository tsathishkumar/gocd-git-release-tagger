package com.thoughtworks.go.release_tagger.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitReleaseTaggerTest {
    private GitReleaseTagger gitReleaseTagger;

    @Before
    public void setUp() throws Exception {
        gitReleaseTagger = new GitReleaseTagger();

    }

    @Test
    public void shouldReturnUsernameFromGitUrl() throws Exception {
        String gitUrl = "git@github.com:orgname/repo_name.git";
        String username = gitReleaseTagger.getUserFromGitUrl(gitUrl);
        assertEquals("orgname", username);
    }

    @Test
    public void shouldReturnUsernameFromHttpsGitUrl() throws Exception {
        String gitUrl = "https://username:pass@github.com/orgname/repo_2.git";
        String organisation = gitReleaseTagger.getUserFromGitUrl(gitUrl);
        assertEquals("orgname",organisation);
    }

    @Test
    public void shouldReturnRepoNameFromGitUrl() throws Exception {
        String gitUrl = "git@github.com:orgname/repo_name.git";
        String repoName = gitReleaseTagger.getRepoFromGitUrl(gitUrl);
        assertEquals("repo_name", repoName);
    }

    @Test
    public void shouldReturnRepoNameFromHttpsGitUrl() throws Exception {
        String gitUrl = "https://username:pass@github.com/orgname/repo_2.git";
        String repoName = gitReleaseTagger.getRepoFromGitUrl(gitUrl);
        assertEquals("repo_2", repoName);
    }
}