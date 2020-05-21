package web

import (
	"net/http"

	"github.com/gorilla/mux"
	"synyx.de/tales/pkg/project"
)

type server struct {
	router http.Handler
}

// Implements the http.Handler interface
func (s *server) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	s.router.ServeHTTP(w, r)
}

// NewServer creates an http.Handler ready to handle tales requests.
func NewServer(projectDir, resourcesDir string) http.Handler {
	repository := &project.FilesystemRepository{
		ProjectDir: projectDir,
	}

	r := mux.NewRouter()
	r.StrictSlash(true)

	fs := http.FileServer(http.Dir(projectDir))
	r.PathPrefix("/editor").Handler(http.StripPrefix("/editor", fs))
	r.PathPrefix("/presenter").Handler(http.StripPrefix("/presenter", fs))

	api := r.PathPrefix("/api/tales").Subrouter()
	api.HandleFunc("/", listProjects(repository)).Methods("GET")
	api.HandleFunc("/", createProject(repository)).Methods("POST")
	api.HandleFunc("/{slug}", loadProject(repository)).Methods("GET")
	api.HandleFunc("/{slug}", updateProject(repository)).Methods("PUT")
	api.HandleFunc("/{slug}", deleteProject(repository)).Methods("DELETE")
	api.HandleFunc("/{slug}/image", saveProjectImage(repository)).Methods("PUT")

	if resourcesDir != "" {
		r.PathPrefix("/").Handler(http.FileServer(http.Dir(resourcesDir)))
	}

	return &server{
		router: r,
	}
}
