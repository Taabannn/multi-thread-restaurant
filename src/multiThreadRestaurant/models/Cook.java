package multiThreadRestaurant.models;

import service.Simulation;
import service.SimulationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Cook implements Runnable {
	private final String name;

	public Cook(String name) {
		this.name = name;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cook cook = (Cook) o;
        return name.equals(cook.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Cook{" +
                "name='" + name + '\'' +
                '}';
    }

    public void run() {
		Simulation.logEvent(SimulationEvent.cookStarting(this));
        Random rnd = new Random();
		try {
			while(!Thread.interrupted()) {
                if (Simulation.isOrderAvailable(this)) {
                    int orderNum = Simulation.handleCookGetOrderNum(this);
                    List<Food> order = Simulation.handleCookGettingOrder(this);
                    List<Food> rawFoodList = new ArrayList<>();
                    rawFoodList.addAll(order);
                    Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, new ArrayList<>(order), orderNum));
                    while (rawFoodList.size() > 0) {
                        Food rawFood = rawFoodList.remove(rnd.nextInt(rawFoodList.size()));
                        if (!Simulation.machines.get(rawFood.name).makeFood(this, orderNum)) {
                            rawFoodList.add(rawFood);
                        }
                        Food food = order.remove(rnd.nextInt(order.size()));
                        if (!Simulation.checkCookingStatus(this, orderNum, food)) {
                            order.add(food);
                        }
                    }
                    while(order.size() > 0) {
                        Food food = order.remove(rnd.nextInt(order.size()));
                        if (!Simulation.checkCookingStatus(this, orderNum, food)) {
                            order.add(food);
                        }
                    }

                    Simulation.handleCompletedOrder(this, orderNum);
                }
			}
            Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
		catch(InterruptedException e) {
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}
}