package web

import (
	"net/http"

	"synyx.de/tales/pkg/project"
)

type server struct {
	router     http.Handler
	repository *project.FilesystemRepository
}

// Implements the http.Handler interface
func (s *server) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	s.router.ServeHTTP(w, r)
}

// NewServer creates a http.Handler ready to handle tales requests.
func NewServer(projectsDir, resourcesDir string) http.Handler {
	r := http.NewServeMux()
	repository := &project.FilesystemRepository{
		ProjectDir: projectsDir,
	}
	s := &server{
		router:     r,
		repository: repository,
	}

	fs := http.FileServer(http.Dir(projectsDir))
	r.Handle("GET /editor/", http.StripPrefix("/editor", fs))
	r.Handle("GET /presenter/", http.StripPrefix("/presenter", fs))

	r.HandleFunc("GET /api/tales/", s.listProjects)
	r.HandleFunc("POST /api/tales/", s.createProject)
	r.HandleFunc("GET /api/tales/{slug}", s.loadProject)
	r.HandleFunc("PUT /api/tales/{slug}", s.updateProject)
	r.HandleFunc("DELETE /api/tales/{slug}", s.deleteProject)
	r.HandleFunc("PUT /api/tales/{slug}/image", s.saveProjectImage)

	r.Handle("GET /", http.FileServer(http.Dir(resourcesDir)))

	return s
}
