package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"path"
	"time"

	"synyx.de/tales/pkg/buildinfo"
	"synyx.de/tales/pkg/web"
)

func init() {
	log.SetFlags(0)
}

func main() {
	log.Printf("Starting tales-server %s (%s)",
		buildinfo.Version,
		buildinfo.FormattedGitSHA())

	shutdownTimeout := 5 * time.Second
	projectDir := defaultProjectDir()
	resourcesDir := "./public"
	log.Printf("Project directory is at \"%s\"", projectDir)

	server := http.Server{
		Handler:      web.NewServer(projectDir, resourcesDir),
		Addr:         "127.0.0.1:3000",
		WriteTimeout: 10 * time.Second,
		ReadTimeout:  10 * time.Second,
	}

	go func() {
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Failed to listen: %s", err)
		}
	}()

	log.Printf("Listening on http://%s/", server.Addr)

	waitForInterrupt()

	log.Printf("Shutting down... (will timeout in %v)", shutdownTimeout)

	ctx, cancel := context.WithTimeout(context.Background(), shutdownTimeout)
	defer cancel()

	if err := server.Shutdown(ctx); err != nil {
		log.Printf("Server shutdown failed: %s", err)
	} else {
		log.Println("Server gracefully shutdown.")
	}
}

func defaultProjectDir() string {
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
