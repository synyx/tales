package buildinfo

import "testing"

func TestCleanState(t *testing.T) {
	Version = "1.0"
	GitSHA = "2547ce676b1f899d08c61c5367c635c76505b443"
	GitTreeState = "clean"

	if sha := FormattedGitSHA(); sha != GitSHA {
		t.Errorf("git sha should be %s, got %s", GitSHA, sha)
	}
}

func TestDirtyState(t *testing.T) {
	Version = "1.0"
	GitSHA = "2547ce676b1f899d08c61c5367c635c76505b443"
	GitTreeState = "dirty"

	if sha := FormattedGitSHA(); sha != GitSHA+"-dirty" {
		t.Errorf("git sha should be %s, got %s", GitSHA, sha)
	}
}
