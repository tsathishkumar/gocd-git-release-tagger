Git Release Tagger
=========================

ThoughtWorks Go CD plugin to automatically tag the release candidate repositories, post release. It will go through the pipeline value stream map to get dependent git repositories and respective commit hashes for tagging.

Download dist/git-release-tagger.jar and place it under <go-server>/plugins/externals and then restart the server.

The following environment variables should be set in the pipeline for this plugin to work.

- GO_USER_NAME - Go user name to get pipeline info
- GO_PASSWORD - Go user password
- GO_USER_EMAIL - Email id to be used in the Git Tag info
- GIT_AUTH_TOKEN - Git auth token for the git repositories

