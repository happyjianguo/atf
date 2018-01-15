package jie.atf.demo.dao;

import java.util.HashMap;
import java.util.Map;

import jie.atf.core.dao.IAtWorkflowDAO;
import jie.atf.core.domain.AtWorkflow;
import jie.atf.core.utils.stereotype.AtfDemo;

@AtfDemo
public class DemoAtWorkflowDAO implements IAtWorkflowDAO {
	private Map<String, Object> repo = new HashMap<String, Object>();
	private static Long id = 1L;

	@Override
	public void create(AtWorkflow workflow) {
		workflow.setId(id++);
		repo.put(workflow.getName(), workflow);
	}

	@Override
	public AtWorkflow find(String name) {
		return (AtWorkflow) repo.get(name);
	}

	@Override
	public void update(AtWorkflow workflow) {
		repo.put(workflow.getName(), workflow);
	}
}
