package jie.atf.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jie.atf.core.api.IAtfMetadataSvs;
import jie.atf.core.dao.IAtfMetadataDAO;
import jie.atf.core.dto.AtWorkflowMetadata;
import jie.atf.core.utils.AtfUtils;
import jie.atf.core.utils.exception.AtfException;

@Service
public class AtfMetadataSvs implements IAtfMetadataSvs {
	@Autowired
	private IAtfMetadataDAO metadataDAO;

	@Override
	public void register(AtWorkflowMetadata workflowMetadata) throws AtfException {
		String name = workflowMetadata.getName();
		AtfUtils.checkNotNull(name, "name can NOT be null");
		AtWorkflowMetadata tmp = metadataDAO.find(name, workflowMetadata.getVersion());
		if (tmp == null) {
			// CREATE
			metadataDAO.create(workflowMetadata);
		} else {
			// UPDATE
			workflowMetadata.setId(tmp.getId());
			metadataDAO.update(workflowMetadata);
		}
	}

	@Override
	public void update(AtWorkflowMetadata workflowMetadata) throws AtfException {
		AtWorkflowMetadata tmp = metadataDAO.find(workflowMetadata.getName(), workflowMetadata.getVersion());
		AtfUtils.checkNotNull(tmp, "workflowMetadata NOT found");
		workflowMetadata.setId(tmp.getId());
		metadataDAO.update(workflowMetadata);
	}

	@Override
	public AtWorkflowMetadata find(String name, Long version) throws AtfException {
		AtfUtils.checkNotNull(name, "name can NOT be null");
		if (version == null)
			return metadataDAO.findLatest(name);
		return metadataDAO.find(name, version);
	}

	@Override
	public AtWorkflowMetadata findLatest(String name) throws AtfException {
		AtfUtils.checkNotNull(name, "name can NOT be null");
		return metadataDAO.findLatest(name);
	}

	@Override
	public AtfMetadataBuilder getMetadataBuilder() {
		return new AtfMetadataBuilder();
	}
}
