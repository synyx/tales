package project

// Dimensions represents and objects width and height.
type Dimensions struct {
	Width  float64 `json:"width"`
	Height float64 `json:"height"`
}

// A Rect describes a rectangles position and size.
type Rect struct {
	X      float64 `json:"x"`
	Y      float64 `json:"y"`
	Width  float64 `json:"width"`
	Height float64 `json:"height"`
}

// A Slide is the set of attributes used to describe a slide.
type Slide struct {
	Rect Rect `json:"rect"`
}

// A Project is the set of attributes used to describe a project.
type Project struct {
	Slug       string     `json:"slug"`
	Name       string     `json:"name"`
	FilePath   string     `json:"file-path" edn:"file-path"`
	Dimensions Dimensions `json:"dimensions"`
	Slides     []Slide    `json:"slides"`
}

// A Repository manages projects.
type Repository interface {
	Exists(string) bool
	LoadProjects() ([]Project, error)
	LoadProject(string) (Project, error)
	SaveProject(string, Project) (Project, error)
	DeleteProject(string) error
	SaveImage(string, string, []byte) (Project, error)
}
