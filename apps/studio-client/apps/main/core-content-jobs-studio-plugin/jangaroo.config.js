const { jangarooConfig } = require("@jangaroo/core");

module.exports = jangarooConfig({
  type: "code",
  sencha: {
    name: "com.coremedia.blueprint__core-content-jobs-studio-plugin",
    namespace: "de.bas.content.studio",
    studioPlugins: [
      {
        mainClass: "de.bas.content.studio.ContentJobStudioPlugin",
        name: "ContentJob Extension",
      },
    ],
  },
  command: {
    build: {
      ignoreTypeErrors: true
    },
  },
});
