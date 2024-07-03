package foo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["foo"], proxyBeanMethods = false)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
