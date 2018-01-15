package jie.atf.demo.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import jie.atf.core.dao.IAtfMetadataDAO;
import jie.atf.core.dto.AtWorkflowMetadata;
import jie.atf.core.utils.stereotype.AtfDemo;

@AtfDemo
public class DemoAtfMetadataDAO implements IAtfMetadataDAO {
	private Map<String, Object> repo = new HashMap<String, Object>();
	private static Long id = 1L;

	@Override
	public void create(AtWorkflowMetadata workflowMetadata) {
		if (workflowMetadata.getVersion() == null)
			workflowMetadata.setVersion(1L);
		workflowMetadata.setId(id++);
		workflowMetadata.setCreateDate(new Date().getTime());
		String key = workflowMetadata.getName() + ":v" + workflowMetadata.getVersion();
		repo.put(key, workflowMetadata);
	}

	@Override
	public AtWorkflowMetadata findLatest(String name) {
		List<AtWorkflowMetadata> lst = findAllVersions(name);
		if (CollectionUtils.isEmpty(lst))
			return null;
		// 名称:v版本号按字典序
		Collections.sort(lst, new Comparator<AtWorkflowMetadata>() {
			@Override
			public int compare(AtWorkflowMetadata lhs, AtWorkflowMetadata rhs) {
				return (lhs.getName() + ":v" + lhs.getVersion()).compareTo(rhs.getName() + ":v" + rhs.getVersion());
			}
		});
		return lst.get(lst.size());
	}

	@Override
	public AtWorkflowMetadata find(String name, Long version) {
		String key = name + ":v" + version;
		return (AtWorkflowMetadata) repo.get(key);
	}

	@Override
	public List<AtWorkflowMetadata> findAllVersions(String name) {
		List<AtWorkflowMetadata> ret = new ArrayList<AtWorkflowMetadata>();
		for (String key : repo.keySet()) {
			if (key.startsWith(name))
				ret.add((AtWorkflowMetadata) repo.get(key));
		}
		return ret;
	}

	@Override
	public void update(AtWorkflowMetadata workflowMetadata) {
		String key = workflowMetadata.getName() + ":v" + workflowMetadata.getVersion();
		workflowMetadata.setUpdateDate(new Date().getTime());
		repo.put(key, workflowMetadata);
	}
}
