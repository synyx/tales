package web

import (
	"io/fs"
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
func NewServer(projectsDir string, resourcesDir fs.FS) http.Handler {
	r := http.NewServeMux()
	repository := &project.FilesystemRepository{
		ProjectDir: projectsDir,
	}
	s := &server{
		router:     r,
		repository: repository,
	}

	projectFileServer := http.FileServer(http.Dir(projectsDir))
	r.Handle("GET /editor/", http.StripPrefix("/editor", projectFileServer))
	r.Handle("GET /presenter/", http.StripPrefix("/presenter", projectFileServer))

	r.HandleFunc("GET /api/tales/", s.listProjects)
	r.HandleFunc("POST /api/tales/", s.createProject)
	r.HandleFunc("GET /api/tales/{slug}", s.loadProject)
	r.HandleFunc("PUT /api/tales/{slug}", s.updateProject)
	r.HandleFunc("DELETE /api/tales/{slug}", s.deleteProject)
	r.HandleFunc("PUT /api/tales/{slug}/image", s.saveProjectImage)

	r.Handle("GET /", http.FileServer(http.FS(resourcesDir)))

	return s
}
