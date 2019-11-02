package apalabrados.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class User {
	
	@Id @Column
	private String userName;
	
	@Column (unique = true)
	private String email;
	
	@Column @JsonIgnore
	private String pwd;
	
	public User() {}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	public String getPwd() {
		return pwd;
	}
	

}
