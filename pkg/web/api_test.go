package web

import (
	"encoding/json"
	"io"
	"net/http"
	"testing"

	"github.com/stretchr/testify/assert"

	"synyx.de/tales/pkg/project"
)

func TestAPI_listProjects(t *testing.T) {
	t.Run("get empty list of projects", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		resp := tc.Request("GET", "/api/tales/", nil)

		assert.Equal(t, 200, resp.StatusCode)
		assert.Equal(t, "application/json; charset=utf-8", resp.Header.Get("Content-Type"))

		var projects []project.Project
		body, _ := io.ReadAll(resp.Body)
		assert.Nil(t, json.Unmarshal(body, &projects))
		assert.Empty(t, projects)
	})
	t.Run("get list of projects", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		_, _ = tc.repo.SaveProject("test-1", project.Project{})
		_, _ = tc.repo.SaveProject("test-2", project.Project{})
		_, _ = tc.repo.SaveProject("test-3", project.Project{})

		resp := tc.Request("GET", "/api/tales/", nil)

		assert.Equal(t, 200, resp.StatusCode)
		assert.Equal(t, "application/json; charset=utf-8", resp.Header.Get("Content-Type"))

		var projects []project.Project
		body, _ := io.ReadAll(resp.Body)
		assert.Nil(t, json.Unmarshal(body, &projects))
		assert.Len(t, projects, 3)
	})
	t.Run("internal error", func(t *testing.T) {
		tc := NewTestClient(t)
		tc.Cleanup()

		resp := tc.Request("GET", "/api/tales/", nil)

		AssertHTTPError(t, resp, 500)
	})
}

func TestAPI_createProject(t *testing.T) {
	t.Run("create project by slug", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		p := project.Project{Slug: "foo", Name: "Bar"}
		resp := tc.Request("POST", "/api/tales/", p)

		AssertProjectResponse(t, resp, p, 201)
	})
	t.Run("create project by name", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		p := project.Project{Name: "Foo Bar"}
		resp := tc.Request("POST", "/api/tales/", p)

		AssertProjectResponse(t, resp, project.Project{Slug: "foo-bar", Name: "Foo Bar"}, 201)
	})
	t.Run("conflicting project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		_, _ = tc.repo.SaveProject("foo", project.Project{Slug: "foo", Name: "Bar"})

		p := project.Project{Slug: "foo", Name: "Bar"}
		resp := tc.Request("POST", "/api/tales/", p)

		AssertError(t, resp, "project already exists\n", 409)
	})
	t.Run("invalid project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		resp := tc.Request("POST", "/api/tales/", nil)

		AssertError(t, resp, "invalid project\n", 400)
	})
	t.Run("unnamed project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		resp := tc.Request("POST", "/api/tales/", project.Project{})

		AssertError(t, resp, "invalid project\n", 400)
	})
	t.Run("internal error", func(t *testing.T) {
		tc := NewTestClient(t)
		tc.Cleanup()

		resp := tc.Request("POST", "/api/tales/", project.Project{Slug: "foo"})

		AssertError(t, resp, tc.dir+" not accessible", 500)
	})
}

func TestAPI_loadProject(t *testing.T) {
	t.Run("existing project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		p := project.Project{Slug: "foo"}
		_, _ = tc.repo.SaveProject("foo", p)

		resp := tc.Request("GET", "/api/tales/foo", nil)

		AssertProjectResponse(t, resp, p, 200)
	})
	t.Run("unknown project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		resp := tc.Request("GET", "/api/tales/foo", nil)

		AssertError(t, resp, "project not found\n", 404)
	})
	t.Run("internal error", func(t *testing.T) {
		tc := NewTestClient(t)
		tc.Cleanup()

		resp := tc.Request("GET", "/api/tales/foo", nil)

		AssertError(t, resp, tc.dir+" not accessible", 500)
	})
}

func TestAPI_updateProject(t *testing.T) {
	t.Run("update project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		_, _ = tc.repo.SaveProject("foo", project.Project{Slug: "foo", Name: "Bar"})

		p := project.Project{Slug: "foo", Name: "Baz"}
		resp := tc.Request("PUT", "/api/tales/foo", p)

		AssertProjectResponse(t, resp, p, 202)
	})
	t.Run("invalid project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		resp := tc.Request("PUT", "/api/tales/foo", nil)

		AssertError(t, resp, "invalid project", 400)
	})
	t.Run("unknown project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		p := project.Project{Slug: "foo"}
		resp := tc.Request("PUT", "/api/tales/foo", p)

		AssertError(t, resp, "project not found\n", 404)
	})
	t.Run("internal error", func(t *testing.T) {
		tc := NewTestClient(t)
		tc.Cleanup()

		resp := tc.Request("PUT", "/api/tales/foo", project.Project{Slug: "foo"})

		AssertError(t, resp, tc.dir+" not accessible", 500)
	})
}

func TestAPI_deleteProject(t *testing.T) {
	t.Run("existing project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		_, _ = tc.repo.SaveProject("foo", project.Project{Slug: "foo"})

		resp := tc.Request("DELETE", "/api/tales/foo", nil)

		assert.Equal(t, 202, resp.StatusCode)

		body, _ := io.ReadAll(resp.Body)
		assert.Empty(t, string(body))
	})
	t.Run("unknown project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		resp := tc.Request("DELETE", "/api/tales/foo", nil)

		AssertError(t, resp, "project not found", 404)
	})
	t.Run("internal error", func(t *testing.T) {
		tc := NewTestClient(t)
		tc.Cleanup()

		resp := tc.Request("DELETE", "/api/tales/foo", nil)

		AssertError(t, resp, tc.dir+" not accessible", 500)
	})
}

func TestAPI_updateProjectImage(t *testing.T) {
	t.Run("update project image", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		_, _ = tc.repo.SaveProject("foo", project.Project{Slug: "foo", Name: "Bar"})

		data := []byte{'f', 'o', 'o'}
		header := http.Header(map[string][]string{})
		header.Add("Content-Type", "image/bmp")
		resp := tc.RequestWithHeaders("PUT", "/api/tales/foo/image", data, header)

		AssertProjectResponse(t, resp, project.Project{Slug: "foo", Name: "Bar", FilePath: "foo.bmp",
			FileType: "image/bmp"}, 202)
	})
	t.Run("invalid content type", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		_, _ = tc.repo.SaveProject("foo", project.Project{Slug: "foo", Name: "Bar"})

		resp := tc.Request("PUT", "/api/tales/foo/image", nil)

		AssertError(t, resp, "unsupported content-type", 500)
	})
	t.Run("unknown project", func(t *testing.T) {
		tc := NewTestClient(t)
		defer tc.Cleanup()

		resp := tc.Request("PUT", "/api/tales/foo/image", nil)

		AssertError(t, resp, "project not found", 404)
	})
}

func AssertProjectResponse(t *testing.T, resp *http.Response, expected project.Project, statusCode int) {
	assert.Equal(t, statusCode, resp.StatusCode)
	assert.Equal(t, "application/json; charset=utf-8", resp.Header.Get("Content-Type"))

	var received project.Project
	body, _ := io.ReadAll(resp.Body)
	assert.NoError(t, json.Unmarshal(body, &received))
	assert.Equal(t, expected, received)
}

func AssertError(t *testing.T, resp *http.Response, errorMsg string, statusCode int) {
	AssertHTTPError(t, resp, statusCode)
	body, _ := io.ReadAll(resp.Body)
	assert.Contains(t, string(body), errorMsg)
}

func AssertHTTPError(t *testing.T, resp *http.Response, statusCode int) {
	assert.Equal(t, statusCode, resp.StatusCode)
	assert.Equal(t, "text/plain; charset=utf-8", resp.Header.Get("Content-Type"))
}
