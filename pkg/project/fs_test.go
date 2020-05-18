package project

import (
	"io/ioutil"
	"os"
	"path"
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestFilesystemRepository_Exists(t *testing.T) {
	repo := testRepo(t)
	defer os.RemoveAll(repo.ProjectDir)

	t.Run("checks existence", func(t *testing.T) {
		assert.False(t, repo.Exists("project-1"))
		repo.SaveProject("project-1", Project{Name: "Project 1"})
		assert.True(t, repo.Exists("project-1"))
	})
}

func TestFilesystemRepository_LoadProjects(t *testing.T) {

	t.Run("load projects", func(t *testing.T) {
		repo := testRepo(t)
		defer os.RemoveAll(repo.ProjectDir)

		project1 := Project{Name: "Project 1"}
		repo.SaveProject("project-1", project1)
		project2 := Project{Name: "Project 2"}
		repo.SaveProject("project-2", project2)
		project3 := Project{Name: "Project 3"}
		repo.SaveProject("project-3", project3)

		projects, err := repo.LoadProjects()
		assert.Nil(t, err)
		assert.Equal(t, 3, len(projects))
		assert.Equal(t, project1, projects[0])
		assert.Equal(t, project2, projects[1])
		assert.Equal(t, project3, projects[2])
	})
	t.Run("non-existing repo directory", func(t *testing.T) {
		repo := testRepo(t)
		os.RemoveAll(repo.ProjectDir)

		projects, err := repo.LoadProjects()
		assert.NotNil(t, err)
		assert.Equal(t, 0, len(projects))
	})
	t.Run("empty repo directory", func(t *testing.T) {
		repo := testRepo(t)
		defer os.RemoveAll(repo.ProjectDir)

		projects, err := repo.LoadProjects()
		assert.Nil(t, err)
		assert.Equal(t, 0, len(projects))
	})
	t.Run("invalid projects", func(t *testing.T) {
		repo := testRepo(t)
		defer os.RemoveAll(repo.ProjectDir)

		{
			// valid config file (empty project)
			filename := path.Join(repo.ProjectDir, "project", "config.json")
			err := os.MkdirAll(filepath.Dir(filename), os.ModePerm)
			assert.Nil(t, err)
			err = ioutil.WriteFile(filename, []byte{'{', '}'}, 0644)
			assert.Nil(t, err)
		}

		{
			// empty config file
			filename := path.Join(repo.ProjectDir, "foo", "config.json")
			err := os.MkdirAll(filepath.Dir(filename), os.ModePerm)
			assert.Nil(t, err)
			err = ioutil.WriteFile(filename, []byte{}, 0644)
			assert.Nil(t, err)
		}

		{
			// unreadable config file
			filename := path.Join(repo.ProjectDir, "bar", "config.json")
			err := os.MkdirAll(filepath.Dir(filename), os.ModePerm)
			assert.Nil(t, err)
			err = ioutil.WriteFile(filename, []byte{}, 0222)
			assert.Nil(t, err)
		}

		{
			// file instead of directory
			filename := path.Join(repo.ProjectDir, "baz")
			err := ioutil.WriteFile(filename, []byte{}, 0644)
			assert.Nil(t, err)
		}

		projects, err := repo.LoadProjects()
		assert.Nil(t, err)
		assert.Equal(t, 1, len(projects))
	})
}

func TestFilesystemRepository_LoadProject(t *testing.T) {
	repo := testRepo(t)
	defer os.RemoveAll(repo.ProjectDir)

	t.Run("load empty slug project", func(t *testing.T) {
		_, err := repo.LoadProject("")
		assert.NotNil(t, err)
	})
	t.Run("load non-existing project", func(t *testing.T) {
		_, err := repo.LoadProject("foo")
		assert.NotNil(t, err)
	})
	t.Run("load existing project", func(t *testing.T) {
		project1 := Project{Name: "Project 1"}
		repo.SaveProject("project-1", project1)
		project, err := repo.LoadProject("project-1")
		assert.Nil(t, err)
		assert.Equal(t, project1, project)
	})
}

func TestFilesystemRepository_SaveProject(t *testing.T) {
	repo := testRepo(t)
	defer os.RemoveAll(repo.ProjectDir)

	t.Run("save empty slug project", func(t *testing.T) {
		project := Project{Name: "Test"}
		savedProject, err := repo.SaveProject("", project)
		assert.NotNil(t, err)
		assert.Equal(t, Project{}, savedProject)
	})
	t.Run("save valid project", func(t *testing.T) {
		project := Project{Name: "Foo"}
		savedProject, err := repo.SaveProject("foo", project)
		assert.Nil(t, err)
		assert.Equal(t, project, savedProject)
	})
}

func TestFilesystemRepository_DeleteProject(t *testing.T) {
	repo := testRepo(t)
	defer os.RemoveAll(repo.ProjectDir)

	t.Run("delete empty slug project", func(t *testing.T) {
		err := repo.DeleteProject("")
		assert.NotNil(t, err)
	})
	t.Run("delete valid project", func(t *testing.T) {
		project := Project{Name: "Foo"}
		repo.SaveProject("foo", project)
		_, err := repo.LoadProject("foo")
		assert.Nil(t, err)
		err = repo.DeleteProject("foo")
		assert.Nil(t, err)
		_, err = repo.LoadProject("foo")
		assert.NotNil(t, err)
	})
}

func testRepo(t *testing.T) FilesystemRepository {
	dir, err := ioutil.TempDir("", "tales-test")
	assert.Nil(t, err)
	return FilesystemRepository{dir}
}
