package jie.atf.demo.domain;

import jie.atf.core.domain.AtTask;
import jie.atf.core.utils.AtfUtils;
import jie.atf.core.utils.exception.AtfException;

/**
 * Groovy脚本类任务demo
 * 
 * @author Jie
 *
 */
public class DemoAtTaskScript extends AtTask {
	@Override
	protected void doExecute() throws AtfException {
		AtfUtils.checkNotNull(groovyCondition, "groovyCondition can NOT be null or empty");
		AtfUtils.checkNotNull(groovyBody, "groovyBody can NOT be null or empty");
		try {
			Boolean condition = (Boolean) AtfUtils.executeGroovyScript(getGroovyCondition(), getInputData());
			if (condition) {
				AtfUtils.executeGroovyScript(groovyBody, getOutputData());
				statusCompleted();
			} else
				statusFailed();
		} catch (Exception e) {
			throw new AtfException(e);
		}
	}
}
