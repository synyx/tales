package web

import (
	"net/http"

	"synyx.de/tales/pkg/project"
)

type server struct {
	router http.Handler
}

// Implements the http.Handler interface
func (s *server) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	s.router.ServeHTTP(w, r)
}

// NewServer creates a http.Handler ready to handle tales requests.
func NewServer(projectsDir, resourcesDir string) http.Handler {
	repository := &project.FilesystemRepository{
		ProjectDir: projectsDir,
	}

	r := http.NewServeMux()

	fs := http.FileServer(http.Dir(projectsDir))
	r.Handle("GET /editor/", http.StripPrefix("/editor", fs))
	r.Handle("GET /presenter/", http.StripPrefix("/presenter", fs))

	r.HandleFunc("GET /api/tales/", listProjects(repository))
	r.HandleFunc("POST /api/tales/", createProject(repository))
	r.HandleFunc("GET /api/tales/{slug}", loadProject(repository))
	r.HandleFunc("PUT /api/tales/{slug}", updateProject(repository))
	r.HandleFunc("DELETE /api/tales/{slug}", deleteProject(repository))
	r.HandleFunc("PUT /api/tales/{slug}/image", saveProjectImage(repository))

	r.Handle("GET /", http.FileServer(http.Dir(resourcesDir)))

	return &server{
		router: r,
	}
}
