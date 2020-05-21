package main

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"os"
	"path"

	"olympos.io/encoding/edn"

	"synyx.de/tales/pkg/buildinfo"
	"synyx.de/tales/pkg/project"
)

func init() {
	log.SetFlags(0)
}

func main() {
	log.Printf("Starting tales-migration %s (%s)",
		buildinfo.Version,
		buildinfo.FormattedGitSHA())

	projectDir := defaultProjectDir()
	log.Printf("Project directory is at \"%s\"", projectDir)

	total := 0
	success := 0

	files, err := ioutil.ReadDir(projectDir)
	if err != nil {
		log.Fatalf("Failed to open project directory: %v", err)
	}

	for _, f := range files {
		if !f.IsDir() {
			continue
		}

		slug := f.Name()
		jsonFile := path.Join(projectDir, slug, "config.json")
		ednFile := path.Join(projectDir, slug, "config.edn")

		_, err := os.Stat(jsonFile)
		if os.IsNotExist(err) {
			log.Printf("Migrating %s", slug)
			err = migrate(slug, ednFile, jsonFile)
			if err != nil {
				log.Printf("Failed to migrate %s: %v", slug, err)
			} else {
				success++
			}
			total++
		}
	}

	log.Printf("Migration completed (%d/%d)", success, total)
}

func migrate(slug, ednFile, jsonFile string) error {
	var p project.Project
	var err error
	var data []byte

	data, err = ioutil.ReadFile(ednFile)
	if err != nil {
		return err
	}

	err = edn.Unmarshal(data, &p)
	if err != nil {
		return err
	}

	p.Slug = slug
	data, err = json.Marshal(p)
	if err != nil {
		return err
	}

	return ioutil.WriteFile(jsonFile, data, 0644)

}

func defaultProjectDir() string {
	homeDir, err := os.UserHomeDir()
	if err != nil {
		log.Fatalf("Failed to retrieve project dir: %v", err)
	}
	return path.Join(homeDir, "Tales")
}
