/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.knockout;

/**
 *
 * @author hhfrancois
 */
public class Ticket {
	private String name;
	private double price;

	public Ticket(String name, double price) {
		this.name = name;
		this.price = price;
	}

	public Ticket() {
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	
}
