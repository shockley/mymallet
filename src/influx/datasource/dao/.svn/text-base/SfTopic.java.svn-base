package influx.datasource.dao;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Dao class for topic
 * @author Shockley
 *
 */
@Entity
@Table(name = "sf_topic")
public class SfTopic {
	@Id @GeneratedValue
	@Column(name = "id")
	public long id;

	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "proj_id") //necessary
	@Basic(fetch = FetchType.LAZY)
	private Project project;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "date_collected")
	private Date timeStamp;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
}
