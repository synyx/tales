package web

import (
	"net/http"

	"github.com/gorilla/mux"
)

type server struct {
	router http.Handler
}

// Implements the http.Handler interface
func (s *server) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	s.router.ServeHTTP(w, r)
}

// NewServer creates an http.Handler ready to handle tales requests.
func NewServer(projectDir string) http.Handler {
	r := mux.NewRouter()
	r.StrictSlash(true)

	fs := http.FileServer(http.Dir(projectDir))
	r.PathPrefix("/editor").Handler(http.StripPrefix("/editor", fs))
	r.PathPrefix("/presenter").Handler(http.StripPrefix("/presenter", fs))

	noopHandler := func(http.ResponseWriter, *http.Request) {}
	api := r.PathPrefix("/api").Subrouter()
	api.HandleFunc("/", noopHandler).Methods("GET")
	api.HandleFunc("/", noopHandler).Methods("POST")
	api.HandleFunc("/{slug}", noopHandler).Methods("GET")
	api.HandleFunc("/{slug}", noopHandler).Methods("PUT")
	api.HandleFunc("/{slug}", noopHandler).Methods("DELETE")
	api.HandleFunc("/{slug}/image", noopHandler).Methods("PUT")

	return &server{
		router: r,
	}
}
