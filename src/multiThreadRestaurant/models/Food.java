package multiThreadRestaurant.models;

import java.util.Objects;

public class Food {
	public final String name;
	public final int cookTimeS;

	public Food(String name, int cookTimeS) {
		this.name = name;
		this.cookTimeS = cookTimeS;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Food food = (Food) o;
		return cookTimeS == food.cookTimeS && name.equals(food.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, cookTimeS);
	}

	@Override
	public String toString() {
		return "Food{" +
				"name='" + name + '\'' +
				", cookTimeS=" + cookTimeS +
				'}';
	}
}