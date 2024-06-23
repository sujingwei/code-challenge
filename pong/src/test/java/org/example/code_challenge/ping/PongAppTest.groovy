package org.example.code_challenge.ping

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification
import java.util.concurrent.CountDownLatch

@SpringBootTest(classes = PongApp.class)
@AutoConfigureWebTestClient
class PongAppTest extends Specification {

    @Autowired
    WebTestClient webTestClient

    def "test pong server"() {
        setup:
        def results = []
        when:
        def requestNum = 20
        def latch = new CountDownLatch(requestNum)
        def threads = []
        for (int i = 0; i < requestNum; i++) {
            threads << new Thread({
                try {
                    // 随机睡眠时间
                    int sleepTime = (int) (Math.random() * 3000)
                    Thread.sleep(sleepTime)
                    results.add(webTestClient.get().uri("/").exchange());
                } finally {
                    latch.countDown()
                }
            })
        }
        threads.each { it.start() }
        latch.await()

        then:
        results.each { result ->
            {
                println "Response status code: ${result.expectStatus()}, Response body: ${result.expectBody(String).returnResult().responseBody}"
            }
        }
        println "finish."
    }

}
