package jie.atf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import jie.atf.core.utils.AtfUtils;

@SpringBootApplication
public class AtfApplication {
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(AtfApplication.class, args);
		// 持有Spring应用上下文
		AtfUtils.setContext(context);
	}
}
