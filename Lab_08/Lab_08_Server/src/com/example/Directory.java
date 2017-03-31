package com.example;

import java.util.ArrayList;
import java.util.Collection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Directory {
	//actually has methods
	private Gson g = new Gson();
	private ArrayList<Employee> employees = new ArrayList<Employee>();
	private String stuff =  "";
	
<<<<<<< HEAD
	public boolean add(String lN, String fN, String pN, String d, String g, String t){
		Employee e = new Employee(lN, fN, pN, d, g, t);
=======
	public boolean add(String lN, String fN, String pN, String d){
		Employee e = new Employee(lN, fN, pN, d, "", "");
>>>>>>> branch 'master' of https://github.com/Toxiguana/chainsaw-361.git
		employees.add(e);
		
		return true;
	}
	
	public void end(){
		stuff = g.toJson(employees);
	}
	
	public void print(){
		//print things
		ArrayList<Employee> em = (g.fromJson(stuff, new TypeToken<Collection<Employee>>(){}.getType()));
		EmployeeComparator ec = new EmployeeComparator();
		em.sort(ec);
		if(em != null && !em.isEmpty()){
			for(Employee e: em) {
				System.out.println(e);
			}
			System.out.println("");
		}
		else{
			System.out.println("No elements in the MainDirectory");
		}
	}
	
	public void clear(){
		employees = new ArrayList<Employee>();
		stuff = g.toJson(employees);
	}
}
