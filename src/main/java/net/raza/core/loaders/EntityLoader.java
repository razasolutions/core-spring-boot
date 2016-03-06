package net.raza.core.loaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class EntityLoader {

	@Autowired
	private UserLoader userLoader;
	@Autowired
	private ProductLoader productLoader;

	public void doLoad() {

		userLoader.doLoad();
		productLoader.doLoad();
		
	}
}
