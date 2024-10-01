package main

import (
	"context"
	"errors"
	"flag"
	"io/fs"
	"log"
	"net/http"
	"os"
	"os/signal"
	"path"
	"path/filepath"
	"time"

	"synyx.de/tales/pkg/buildinfo"
	"synyx.de/tales/pkg/web"
)

var (
	resourcesDir string
	projectsDir  string
)

func init() {
	log.SetFlags(0)
}

func main() {
	flag.StringVar(&resourcesDir, "resources", "", "path to public resources")
	flag.StringVar(&projectsDir, "projects", defaultProjectsDir(), "path to projects")
	flag.Parse()

	log.Printf("Starting tales-server %s (%s)",
		buildinfo.Version,
		buildinfo.FormattedGitSHA())

	projectsDir, err := filepath.Abs(projectsDir)
	if err != nil {
		log.Fatal(err)
	}

	log.Printf("Projects directory is at \"%s\"", projectsDir)

	var resourceFS fs.FS
	if resourcesDir != "" {
		resourcesDir, err := filepath.Abs(resourcesDir)
		if err != nil {
			log.Fatal(err)
		}

		log.Printf("Resources directory is at \"%s\"", resourcesDir)
		resourceFS = os.DirFS(resourcesDir)
	} else {
		resourceFS, _ = fs.Sub(web.EmbeddedResources, "public")
		log.Println("Using embedded resources")
	}

	server := http.Server{
		Handler:      web.NewServer(projectsDir, resourceFS),
		Addr:         "127.0.0.1:3000",
		WriteTimeout: 10 * time.Second,
		ReadTimeout:  10 * time.Second,
	}

	go func() {
		if err := server.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Fatalf("Failed to listen: %s", err)
		}
	}()

	log.Printf("Listening on http://%s/", server.Addr)

	waitForInterrupt()

	shutdownTimeout := 5 * time.Second
	log.Printf("Shutting down... (will timeout in %v)", shutdownTimeout)

	ctx, cancel := context.WithTimeout(context.Background(), shutdownTimeout)
	defer cancel()

	if err := server.Shutdown(ctx); err != nil {
		log.Printf("Server shutdown failed: %s", err)
	} else {
		log.Println("Server gracefully shutdown.")
	}
}

func defaultProjectsDir() string {
	homeDir, err := os.UserHomeDir()
	if err != nil {
		log.Fatalf("Failed to retrieve project dir: %v", err)
	}
	return path.Join(homeDir, "Tales")
}

func waitForInterrupt() {
	trap := make(chan os.Signal, 1)
	signal.Notify(trap, os.Interrupt)
	<-trap
}
