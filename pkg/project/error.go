package project

var (
	// ErrNotExist is returned by Repository in case a project does not exist.
	ErrNotExist = &Error{"does not exist"}
)

// Error represents a project repository error.
type Error struct {
	ErrorString string
}

func (e *Error) Error() string {
	return e.ErrorString
}
