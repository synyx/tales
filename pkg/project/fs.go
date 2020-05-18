package project

import (
	"encoding/json"
	"errors"
	"io/ioutil"
	"log"
	"os"
	"path"
	"path/filepath"
)

type FilesystemRepository struct {
	ProjectDir string
}

func (fr *FilesystemRepository) configFile(slug string) string {
	return path.Join(fr.ProjectDir, slug, "config.json")
}

func (fr *FilesystemRepository) Exists(slug string) bool {
	filename := fr.configFile(slug)
	info, err := os.Stat(filename)
	if os.IsNotExist(err) {
		return false
	}
	return !info.IsDir()
}

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

func (fr *FilesystemRepository) DeleteProject(slug string) error {
	if !fr.Exists(slug) {
		return ErrNotExist
	}
	jsonFile := fr.configFile(slug)
	return os.RemoveAll(filepath.Dir(jsonFile))
}
