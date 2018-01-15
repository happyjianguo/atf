package jie.atf.core.dto;

/**
 * 蓝图的共性
 * 
 * @author Jie
 *
 */
public abstract class AtAbstractMetadata {
	private Long id;
	private Long createDate; // 创建时间
	private Long updateDate; // 更新时间

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}
}
