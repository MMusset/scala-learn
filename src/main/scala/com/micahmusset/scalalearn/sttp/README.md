# sttp

- [GitHub](https://github.com/softwaremill/sttp)
- [Documentation](https://sttp.softwaremill.com/en/latest/quickstart.html)

## Getting Started

#### build.sbt
```
"com.softwaremill.sttp.client3" %% "core" % "@VERSION@"
```

#### Basic Example
```
import sttp.client3._

val backend = HttpURLConnectionBackend()
val response = basicRequest
  .body("Hello, world!")  
  .post(uri"https://httpbin.org/post?hello=world").send(backend)

println(response.body)   
```
