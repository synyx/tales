package main

import (
	"log"

	"synyx.de/tales/pkg/buildinfo"
)

func main() {
	log.Printf("Starting tales-server %s (%s)",
		buildinfo.Version,
		buildinfo.FormattedGitSHA())
	log.Println("Server gracefully shutdown.")
}
