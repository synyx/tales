package web

import (
	"encoding/json"
	"io"
	"net/http"

	"github.com/mozillazg/go-slugify"

	"synyx.de/tales/pkg/project"
)

func (s *server) listProjects(w http.ResponseWriter, r *http.Request) {
	projects, err := s.repository.LoadProjects()
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	jsonResponse(w, projects, http.StatusOK)

}

func (s *server) loadProject(w http.ResponseWriter, r *http.Request) {
	slug := r.PathValue("slug")
	if !s.repository.Exists(slug) {
		notFound(w)
		return
	}
	proj, err := s.repository.LoadProject(slug)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	jsonResponse(w, proj, http.StatusOK)

}

func (s *server) createProject(w http.ResponseWriter, r *http.Request) {
	proj, err := extractProject(r)
	if err != nil {
		badRequest(w)
		return
	}
	if proj.Slug == "" {
		proj.Slug = slugify.Slugify(proj.Name)
	}
	if proj.Slug == "" {
		badRequest(w)
		return
	}
	if s.repository.Exists(proj.Slug) {
		conflict(w)
		return
	}
	proj, err = s.repository.SaveProject(proj.Slug, proj)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	jsonResponse(w, proj, http.StatusCreated)

}

func (s *server) updateProject(w http.ResponseWriter, r *http.Request) {
	slug := r.PathValue("slug")
	proj, err := extractProject(r)
	if err != nil {
		badRequest(w)
		return
	}
	if !s.repository.Exists(slug) {
		notFound(w)
		return
	}
	proj, err = s.repository.SaveProject(slug, proj)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	jsonResponse(w, proj, http.StatusAccepted)

}

func (s *server) deleteProject(w http.ResponseWriter, r *http.Request) {
	slug := r.PathValue("slug")
	if !s.repository.Exists(slug) {
		notFound(w)
		return
	}
	err := s.repository.DeleteProject(slug)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	emptyResponse(w, http.StatusAccepted)
}

func (s *server) saveProjectImage(w http.ResponseWriter, r *http.Request) {
	slug := r.PathValue("slug")
	if !s.repository.Exists(slug) {
		notFound(w)
		return
	}
	data, err := io.ReadAll(r.Body)
	if err != nil {
		badRequest(w)
		return
	}
	contentType := r.Header.Get("Content-Type")
	proj, err := s.repository.SaveImage(slug, contentType, data)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	jsonResponse(w, proj, http.StatusAccepted)
	
}

func extractProject(req *http.Request) (project.Project, error) {
	var proj project.Project
	body, err := io.ReadAll(req.Body)
	if err != nil {
		return proj, err
	}
	err = json.Unmarshal(body, &proj)
	if err != nil {
		return proj, err
	}
	return proj, nil
}

func badRequest(w http.ResponseWriter) {
	http.Error(w, "invalid project", http.StatusBadRequest)
}

func conflict(w http.ResponseWriter) {
	http.Error(w, "project already exists", http.StatusConflict)
}

func notFound(w http.ResponseWriter) {
	http.Error(w, "project not found", http.StatusNotFound)
}

func emptyResponse(w http.ResponseWriter, code int) {
	w.WriteHeader(code)
}

func jsonResponse(w http.ResponseWriter, data interface{}, code int) {
	result, err := json.Marshal(data)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(code)

	// intentionally ignore the error, there is no way to signal the client,
	// logging this is also not valuable.
	_, _ = w.Write(result)
}
