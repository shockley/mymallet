package influx.datasource.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "projects")
public class Project {
	@Id @GeneratedValue
	@Column(name = "proj_id")
	private Long id;
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "forge_id") //necessary
	@Basic(fetch = FetchType.LAZY)
	private Forge forge;
	
	@Column(name = "proj_short_name")	
	private String name;
	
	@Column(name = "proj_real_name")	
	private String realname;
	
	
	@Column(name = "url")	
	private String url;
	
	@Column(name = "date_collected")
	private Date timestamp;

	@Column(name = "page_index")
	private int page;
	
	@OneToMany(mappedBy = "project")
	@Basic(fetch = FetchType.LAZY)
	private List<SfSummary> sfSummary;

	@OneToMany(mappedBy = "project")
	@Basic(fetch = FetchType.LAZY)
	private List<SfTopic> sfTopic;
	
	@OneToMany(mappedBy = "project")
	@Basic(fetch = FetchType.LAZY)
	private List<OhlohSummary> ohlohSummary;
	
	@OneToMany(mappedBy = "project")
	@Basic(fetch = FetchType.LAZY)
	private List<OhlohTag> ohlohTag;
	
	public List<OhlohTag> getOhlohTag() {
		return ohlohTag;
	}

	public void setOhlohTag(List<OhlohTag> ohlohTag) {
		this.ohlohTag = ohlohTag;
	}

	public List<OhlohSummary> getOhlohSummary() {
		return ohlohSummary;
	}

	public void setOhlohSummary(List<OhlohSummary> ohlohSummary) {
		this.ohlohSummary = ohlohSummary;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setRealName(String realname) {
		this.realname = realname;
	}

	public String getRealName() {
		return realname;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setForge(Forge forge) {
		this.forge = forge;
	}

	public Forge getForge() {
		return forge;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPage() {
		return page;
	}

	public List<SfSummary> getSfSummary() {
		return sfSummary;
	}

	public void setSfSummary(List<SfSummary> sfSummary) {
		this.sfSummary = sfSummary;
	}

	public List<SfTopic> getSfTopic() {
		return sfTopic;
	}

	public void setSfTopic(List<SfTopic> sfTopic) {
		this.sfTopic = sfTopic;
	}
	
	public boolean equals(Object o){
		if(o==null || !(o instanceof Project)) return false;
		Project tmp = (Project) o;
		return this.id.equals(tmp.id);
	}
	
	public int hashCode(){
		return this.id.intValue();
	}
}
