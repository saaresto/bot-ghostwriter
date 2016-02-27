package com.iissakin.ghostwriter
import com.iissakin.ghostwriter.knowledge.service.WordService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
class GhostwriterApplication {

	@Autowired
	WordService wordService

	static void main(String[] args) {
		SpringApplication.run GhostwriterApplication, args
	}

	@RequestMapping(value = "/word/{content}")
	def newWord(@PathVariable("content") String content) {
		wordService.newWord(content)
	}

	@RequestMapping(value = "/{follower}/follows/{word}")
	def newRelation(@PathVariable("follower") String follower, @PathVariable("word") String word) {
		wordService.newRelation(follower, word)
	}

	@RequestMapping(value = "/get")
	def getWords() {
		wordService.getWords()
	}
}
