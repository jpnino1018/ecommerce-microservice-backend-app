const fs = require("fs");

module.exports = {
  prepare: (pluginConfig, context) => {
    const version = context.nextRelease.version;
    fs.writeFileSync("VERSION", version);
    context.logger.log(`✔ Wrote version ${version} to VERSION file`);
  },
};
