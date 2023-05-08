const proxyValidator = require('mass-proxy-validator')

proxyValidator({
    input: "./proxies.txt",
    output: "./newProxies.txt",
    url: "http://example.com/", //URL to test against,
    threads: 100,
    timeout: 5000
})