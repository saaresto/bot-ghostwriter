package com.iissakin.ghostwriter
import com.iissakin.ghostwriter.knowledge.service.WordService
import com.iissakin.ghostwriter.telegram.job.UpdaterJob
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.annotation.PostConstruct

@SpringBootApplication
@RestController
@EnableScheduling
@Configuration
class GhostwriterApplication {

	@Autowired
	WordService wordService

	@Autowired
	UpdaterJob job

	static void main(String[] args) {
		SpringApplication.run GhostwriterApplication, args
	}

	@PostConstruct
	def poller() {
		new Thread(new Runnable() {
			@Override
			void run() {
				while(true) {
					try {
						job.doJob()
					} catch (Exception e) {
						e.printStackTrace()
					}
				}
			}
		}).start()
	}

	@RequestMapping(value = "/get")
	def getWords() {
		wordService.getWords()
	}
}
