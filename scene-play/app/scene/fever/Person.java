package scene.fever;

import java.io.Serializable;

public class Person implements Serializable {
	
	private int identifier;
	private String name;
	private int temperature;
	
	public Person(String name, int id) {
		this.setName(name);
		this.identifier = id;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public int getTemperature() {
		return temperature;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
