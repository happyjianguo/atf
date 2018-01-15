package jie.atf.core.utils;

import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import jie.atf.core.utils.exception.AtfException;

public class AtfUtils {
	/** 持有Spring应用上下文 */
	private static ApplicationContext context;
	private static ScriptEngine engine = null;

	public static ApplicationContext getContext() {
		return context;
	}

	public static void setContext(ApplicationContext context) {
		AtfUtils.context = context;
	}

	/**
	 * 执行Groovy脚本
	 * 
	 * @param script
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static Object executeGroovyScript(String script, Map<String, Object> param) throws Exception {
		checkNotNull(script, "Groovy script can NOT be null");
		// Groovy脚本import包 + 执行脚本
		String evalScript = AtfConstants.GROOVY_IMPORTS + script;

		ScriptEngine engine = groovyScriptEngine();
		Bindings bindings = engine.createBindings();
		bindings.putAll(param);
		return engine.eval(evalScript, bindings);
	}

	/**
	 * 获取ScriptEngine
	 * 
	 * @return ScriptEngine
	 */
	private static ScriptEngine groovyScriptEngine() {
		if (engine == null) {
			ScriptEngineManager engineManager = new ScriptEngineManager();
			engine = engineManager.getEngineByName("groovy");
		}
		return engine;
	}

	/**
	 * checkNotNull
	 * 
	 * @param reference
	 * @param errorMessage
	 * @throws AtfException
	 */
	public static <T> void checkNotNull(T reference, String errorMessage) throws AtfException {
		if (reference == null)
			throw new AtfException(errorMessage);

		if (reference instanceof String)
			if (StringUtils.isEmpty((String) reference))
				throw new AtfException(errorMessage);
	}
}
