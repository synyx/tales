module.exports = ctx => ({
  plugins: {
    "postcss-import": {},
    cssnano: ctx.file.basename === "viewer.css" ? {} : false,
  },
});
