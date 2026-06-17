package com.shopwavefusion.modal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Size {

	@Column(name = "size_name")
	private String name;

	public Size() {
	}

	public Size(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}