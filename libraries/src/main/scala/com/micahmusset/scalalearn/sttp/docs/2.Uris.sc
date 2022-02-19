import sttp.client3._

// The Uri class is immutable, and can be constructed by hand
// but in many cases the URI interpolator will be easier to use.

// =====================================================================================================================
// URI interpolator
val user   = "Mary Smith"
val filter = "programming languages"

uri"http://example.com/$user/skills?filter=$filter".toString ==
  "http://example.com/Mary%20Smith/skills?filter=programming+languages"

uri"http://example.org/${"a/b"}".toString() == "http://example.org/a%2Fb"

// the embedded / is not escaped
uri"http://example.org/${"a"}/${"b"}".toString() == "http://example.org/a/b"

// the embedded : is not escaped
uri"http://${"example.org:8080"}".toString() == "http://example.org:8080"

// =====================================================================================================================
// Optional Values
val v1 = None
val v2 = Some("v2")

uri"http://example.com?p1=$v1&p2=v2".toString() == "http://example.com?p2=v2"

uri"http://$v1.$v2.example.com".toString() == "http://v2.example.com"

uri"http://example.com#$v1".toString() == "http://example.com"

// =====================================================================================================================
// Maps and sequences

val ps = Map("p1" -> "v1", "p2" -> "v2")
uri"http://example.com?$ps&p3=p4".toString() == "http://example.com?p1=v1&p2=v2&p3=p4"

val params = List("a", "b", "c")
uri"http://example.com/$params".toString() == "http://example.com/a/b/c"

// =====================================================================================================================
// All features combined
val secure     = true
val scheme     = if (secure) "https" else "http"
val subdomains = List("sub1", "sub2")
val vx         = Some("y z")
val paramMap   = Map("a" -> 1, "b" -> 2)
val jumpTo     = Some("section2")

uri"$scheme://$subdomains.example.com?x=$vx&$paramMap#$jumpTo"
  .toString() == "https://sub1.sub2.example.com?x=y+z&a=1&b=2#section2"
