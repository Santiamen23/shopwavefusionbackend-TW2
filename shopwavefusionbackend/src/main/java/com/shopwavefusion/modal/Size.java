package com.shopwavefusion.modal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Size {

	@Column(name = "size_name")
	private String name;

	@Column(name = "size_quantity")
	private int quantity;

	public Size() {
	}

	public Size(String name, int quantity) {
		this.name = name;
		this.quantity = quantity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}

