package web

import (
	"bytes"
	"encoding/json"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"

	"github.com/stretchr/testify/assert"
	"synyx.de/tales/pkg/project"
)

type TestClient struct {
	dir     string
	handler http.Handler
	repo    project.Repository
	assert  *assert.Assertions
}

func NewTestClient(t *testing.T) *TestClient {
	dir, err := ioutil.TempDir("", "tales-test")
	assert.NoError(t, err)
	handler := NewServer(dir, "")
	repo := &project.FilesystemRepository{
		ProjectDir: dir,
	}

	return &TestClient{
		dir:     dir,
		handler: handler,
		repo:    repo,
		assert:  assert.New(t),
	}
}

func (tc *TestClient) Request(method, url string, payload interface{}) *http.Response {
	return tc.RequestWithHeaders(method, url, payload, nil)
}

func (tc *TestClient) RequestWithHeaders(method, url string, payload interface{}, header http.Header) *http.Response {
	var body []byte
	if payload != nil {
		body, _ = json.Marshal(payload)
	}
	req := httptest.NewRequest(method, url, bytes.NewReader(body))
	req.Header = header
	w := httptest.NewRecorder()
	tc.handler.ServeHTTP(w, req)

	return w.Result()
}

func (tc *TestClient) Cleanup() {
	os.RemoveAll(tc.dir)
}

func TestServer_ServeHTTP(t *testing.T) {
	t.Run("http handler", func(t *testing.T) {
		assert.Implements(t, new(http.Handler), NewServer("", ""))
	})
}
