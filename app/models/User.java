package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class User extends Model {
	@Id
	private String username;
	
	private String password;
	
	@OneToMany(mappedBy = "sender", cascade = CascadeType.PERSIST)
	private List<Message> messages = new ArrayList<Message>();
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}
}
