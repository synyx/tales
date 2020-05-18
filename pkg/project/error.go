package project

var (
	ErrNotExist = &ProjectError{"does not exist"}
)

type ProjectError struct {
	ErrorString string
}

func (pe *ProjectError) Error() string {
	return pe.ErrorString
}
