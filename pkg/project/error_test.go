package project

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestError_Error(t *testing.T) {
	t.Run("checks existence", func(t *testing.T) {
		assert.Equal(t, "does not exist", ErrNotExist.Error())
	})
}
