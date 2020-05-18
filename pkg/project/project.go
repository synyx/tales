package project

type Dimensions struct {
	Width  float64 `json:"width"`
	Height float64 `json:"height"`
}
type Rect struct {
	X      float64 `json:"x"`
	Y      float64 `json:"y"`
	Width  float64 `json:"width"`
	Height float64 `json:"height"`
}
type Slide struct {
	Rect Rect `json:"rect"`
}
type Project struct {
	Slug       string     `json:"slug"`
	Name       string     `json:"name"`
	FilePath   string     `json:"file-path" edn:"file-path"`
	Dimensions Dimensions `json:"dimensions"`
	Slides     []Slide    `json:"slides"`
}

type Repository interface {
	Exists(string) bool
	LoadProjects() ([]Project, error)
	LoadProject(string) (Project, error)
	SaveProject(string, Project) (Project, error)
	DeleteProject(string) error
}
