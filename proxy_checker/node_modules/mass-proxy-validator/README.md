## Install

Using NPM:

```
$ npm i mass-proxy-validator
```

Using Yarn:

```
$ yarn add mass-proxy-validator
```

## Example

```javascript
const proxyValidator = require("mass-proxy-validator");

proxyValidator({
  input: "path/to/proxies.txt",
  output: "path/to/newProxies.txt",
  url: "http://example.com/",
  threads: 100,
  timeout: 1000
});
```

## Options

```javascript
{
  input: "proxies.txt",
  /* Path to proxies .txt file.
      Format of proxies file should look like this.

      ...
      96.9.88.54:31447
      96.9.73.80:45984
      96.65.123.249:8118
      95.87.210.100:50451
      95.86.40.170:53281
      ...
    */

  output: "newProxies.txt",
  /* Path of .txt file to append to.
      If it does not exist it will be created.
    */

  url: "http://example.com/",
  // Url to test proxies against.

  threads: 100,
  // Amount of threads to run.

  timeout: 2000
  // Time to wait before closing connection.
}
```
