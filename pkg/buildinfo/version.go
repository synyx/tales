package buildinfo

import "fmt"

var (
	// Version holds the latest git tag.
	Version string
	// GitSHA holds the latest git commit SHA.
	GitSHA string
	// GitTreeState holds the git tree state during build.
	GitTreeState string
)

// FormattedGitSHA returns the latest commit SHA with tree state information.
func FormattedGitSHA() string {
	if GitTreeState != "clean" {
		return fmt.Sprintf("%s-%s", GitSHA, GitTreeState)
	}
	return GitSHA
}
