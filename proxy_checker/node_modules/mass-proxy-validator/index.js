const axios = require("axios");
const readLine = require("readline");
const fs = require("fs");

module.exports = config => {
  let proxies = [];

  var lineReader = readLine.createInterface({
    input: fs.createReadStream(config.input)
  });

  lineReader.on("line", line => {
    proxies.push(line);
  });

  lineReader.on("close", () => {
    let threads = config.threads;
    const split = (proxies.length / threads).toFixed(0);

    for (let i = 1; i <= threads; i++) {
      loop(split, i, () => {
        console.log(`Thread ${i} done.`);
      });
    }
  });

  const loop = async (split, thread, done) => {
    for (var y = split * thread - split; y <= split * thread; y++) {
      let ip_port = proxies[y].split(":");

      await new Promise(resolve => {
        axios({
          method: "get",
          url: config.url,
          proxy: { host: ip_port[0], port: ip_port[1] },
          timeout: config.timeout
        })
          .then(() => {
            resolve();
            fs.appendFile(
              config.output,
              `${ip_port[0]}:${ip_port[1]}\n`,
              err => {
                if (err) throw err;
                console.log(`Saved: ${ip_port[0]}:${ip_port[1]}`);
              }
            );
          })
          .catch(err => {
            resolve();
          });
      });
    }
    done();
  };
};
