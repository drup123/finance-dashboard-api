package com.finance.dashboard.dto;

import com.finance.dashboard.model.Role;
import jakarta.validation.constraints.Size;



public class UpdateUserRequest {

    @Size(max = 100)
    private String name;

    private Role role;

    private Boolean active;

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setName(String name) {
		this.name = name;
	}
}
