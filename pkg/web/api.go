package web

import (
	"encoding/json"
	"io/ioutil"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/mozillazg/go-slugify"
	"synyx.de/tales/pkg/project"
)

func listProjects(repository project.Repository) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		projects, err := repository.LoadProjects()
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		jsonResponse(w, []project.Project(projects), http.StatusOK)
	}
}

func loadProject(repository project.Repository) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		vars := mux.Vars(r)
		slug := vars["slug"]
		if !repository.Exists(slug) {
			notFound(w)
			return
		}
		project, err := repository.LoadProject(slug)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		jsonResponse(w, project, http.StatusOK)
	}
}

func createProject(repository project.Repository) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		project, err := extractProject(r)
		if err != nil {
			badRequest(w)
			return
		}
		if project.Slug == "" {
			project.Slug = slugify.Slugify(project.Name)
		}
		if project.Slug == "" {
			badRequest(w)
			return
		}
		if repository.Exists(project.Slug) {
			conflict(w)
			return
		}
		project, err = repository.SaveProject(project.Slug, project)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		jsonResponse(w, project, http.StatusCreated)
	}
}

func updateProject(repository project.Repository) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		vars := mux.Vars(r)
		slug := vars["slug"]
		project, err := extractProject(r)
		if err != nil {
			badRequest(w)
			return
		}
		if !repository.Exists(slug) {
			notFound(w)
			return
		}
		project, err = repository.SaveProject(slug, project)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		jsonResponse(w, project, http.StatusAccepted)
	}
}

func deleteProject(repository project.Repository) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		vars := mux.Vars(r)
		slug := vars["slug"]
		if !repository.Exists(slug) {
			notFound(w)
			return
		}
		err := repository.DeleteProject(slug)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		emptyResponse(w, http.StatusAccepted)
	}
}

func saveProjectImage(repository project.Repository) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		vars := mux.Vars(r)
		slug := vars["slug"]
		if !repository.Exists(slug) {
			notFound(w)
			return
		}
		data, err := ioutil.ReadAll(r.Body)
		if err != nil {
			badRequest(w)
			return
		}
		contentType := r.Header.Get("Content-Type")
		project, err := repository.SaveImage(slug, contentType, data)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		jsonResponse(w, project, http.StatusAccepted)
	}
}

func extractProject(req *http.Request) (project.Project, error) {
	var project project.Project
	body, err := ioutil.ReadAll(req.Body)
	if err != nil {
		return project, err
	}
	err = json.Unmarshal(body, &project)
	if err != nil {
		return project, err
	}
	return project, nil
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
	w.Write(result)
}
