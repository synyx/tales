package web

import "embed"

// EmbeddedResources holds static web server content.
//
//go:embed public
var EmbeddedResources embed.FS
