package com.iissakin.ghostwriter

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = GhostwriterApplication)
@WebAppConfiguration
class GhostwriterApplicationTests {

	@Autowired
	OrientGraphFactory orientGraphFactory

	@Test
	void contextLoads() {
	}

}
