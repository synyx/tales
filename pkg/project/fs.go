package project

import (
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"path"
	"path/filepath"
)

// A FilesystemRepository manages projects on the local filesystem.
type FilesystemRepository struct {
	ProjectDir string
}

func (fr *FilesystemRepository) configFile(slug string) string {
	return path.Join(fr.ProjectDir, slug, "config.json")
}

func (fr *FilesystemRepository) imageFile(slug, filename string) string {
	return path.Join(fr.ProjectDir, slug, filename)
}

// Exists implements the Repository.Exists method.
func (fr *FilesystemRepository) Exists(slug string) bool {
	filename := fr.configFile(slug)
	info, err := os.Stat(filename)
	if os.IsNotExist(err) {
		return false
	}
	return !info.IsDir()
}

// LoadProjects implements the Repository.LoadProjects method.
func (fr *FilesystemRepository) LoadProjects() ([]Project, error) {
	projects := make([]Project, 0)
	files, err := ioutil.ReadDir(fr.ProjectDir)
	if err != nil {
		return projects, err
	}

	for _, f := range files {
		if !f.IsDir() {
			continue
		}
		slug := f.Name()
		project, err := fr.LoadProject(slug)
		if err != nil {
			log.Printf("Failed to load project: %v", err)
			continue
		}
		projects = append(projects, project)
	}

	return projects, nil
}

// LoadProject implements the Repository.LoadProject method.
func (fr *FilesystemRepository) LoadProject(slug string) (Project, error) {
	if !fr.Exists(slug) {
		return Project{}, ErrNotExist
	}
	jsonFile := fr.configFile(slug)
	data, err := ioutil.ReadFile(jsonFile)
	if err != nil {
		return Project{}, err
	}

	var project Project
	err = json.Unmarshal(data, &project)
	if err != nil {
		return Project{}, err
	}

	return project, nil
}

// SaveProject implements the Repository.SaveProject method.
func (fr *FilesystemRepository) SaveProject(slug string, project Project) (Project, error) {
	if slug == "" {
		return Project{}, errors.New("missing slug")
	}

	jsonFile := fr.configFile(slug)
	err := os.MkdirAll(filepath.Dir(jsonFile), os.ModePerm)
	if err != nil {
		return Project{}, err
	}

	data, err := json.Marshal(project)
	if err != nil {
		return Project{}, err
	}

	return project, ioutil.WriteFile(jsonFile, data, 0644)
}

// DeleteProject implements the Repository.DeleteProject method.
func (fr *FilesystemRepository) DeleteProject(slug string) error {
	if !fr.Exists(slug) {
		return ErrNotExist
	}
	jsonFile := fr.configFile(slug)
	return os.RemoveAll(filepath.Dir(jsonFile))
}

// SaveImage implements the Repository.SaveImage method.
func (fr *FilesystemRepository) SaveImage(slug, contentType string, data []byte) (Project, error) {
	if !fr.Exists(slug) {
		return Project{}, ErrNotExist
	}
	extension := imageType(contentType)
	filename := slug + "." + extension
	if extension == "" {
		return Project{}, fmt.Errorf("unsupported content-type: %v", contentType)
	}
	imageFile := fr.imageFile(slug, filename)
	err := os.MkdirAll(filepath.Dir(imageFile), os.ModePerm)
	if err != nil {
		return Project{}, err
	}
	err = ioutil.WriteFile(imageFile, data, 0644)
	if err != nil {
		return Project{}, err
	}
	project, err := fr.LoadProject(slug)
	if err != nil {
		return Project{}, err
	}
	project.FilePath = filename
	project.FileType = contentType
	project, err = fr.SaveProject(slug, project)
	if err != nil {
		return Project{}, err
	}
	return project, nil
}

func imageType(contentType string) string {
	switch contentType {
	case "image/gif":
		return "gif"
	case "image/png":
		return "png"
	case "image/jpeg":
		return "jpg"
	case "image/bmp":
		return "bmp"
	case "image/svg+xml":
		return "svg"
	}
	return ""
}
