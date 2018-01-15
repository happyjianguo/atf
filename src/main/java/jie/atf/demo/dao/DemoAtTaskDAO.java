package jie.atf.demo.dao;

import java.util.HashMap;
import java.util.Map;

import jie.atf.core.dao.IAtTaskDAO;
import jie.atf.core.domain.AtTask;
import jie.atf.core.utils.stereotype.AtfDemo;

@AtfDemo
public class DemoAtTaskDAO implements IAtTaskDAO {
	private Map<String, Object> repo = new HashMap<String, Object>();
	private static Long id = 1L;

	@Override
	public void create(AtTask task) {
		task.setId(id++);
		repo.put(task.getId().toString(), task);
	}

	@Override
	public AtTask find(Long id) {
		return (AtTask) repo.get(id);
	}

	@Override
	public void update(AtTask task) {
		repo.put(task.getId().toString(), task);
	}
}
