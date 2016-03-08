package com.iissakin.ghostwriter
import com.iissakin.ghostwriter.knowledge.service.WordService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
@EnableScheduling
@Configuration
class GhostwriterApplication {

	@Autowired
	WordService wordService

	static void main(String[] args) {
		SpringApplication.run GhostwriterApplication, args
	}

	@RequestMapping(value = "/get")
	def getWords() {
		wordService.getWords()
	}
}
