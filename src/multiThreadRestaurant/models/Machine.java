package multiThreadRestaurant.models;

import service.Simulation;
import service.SimulationEvent;
import multiThreadRestaurant.enumeration.MachineType;

public class Machine {
	
	public final MachineType machineType;
	public final Food machineFoodType;
	public volatile int itemsCooking = 0;
    private final int capacity;

	public Machine(MachineType machineType, Food food, int capacityIn) {
		this.machineType = machineType;
		this.machineFoodType = food;
		this.capacity = capacityIn;
	}

	public synchronized boolean makeFood(Cook cook, int orderNum) throws InterruptedException {
	    if (itemsCooking < capacity) {
            itemsCooking++;
            Thread cookThread = new Thread(new CookAnItem(orderNum, this));
            Simulation.logEvent(SimulationEvent.cookStartedFood(cook, machineFoodType, orderNum));
            cookThread.start();
            return true;
        }
        return false;
	}

	private class CookAnItem implements Runnable {
	    private final int orderNum;
	    private final Machine machine;

	    CookAnItem(int orderNum, Machine machine) {
	        this.orderNum = orderNum;
	        this.machine = machine;
        }

		public void run() {
			try {
                Simulation.logEvent(SimulationEvent.machineCookingFood(machine, machineFoodType));
                Thread.sleep(machineFoodType.cookTimeS);
                Simulation.updateCookedOrder(machine, orderNum, machineFoodType);

			} catch(InterruptedException e) {
                System.out.println("Cooking thread interrupted.");
            }
		}
	}

	public String toString() {
		switch (machineType) {
			case FOUNTAIN: 		return "Fountain";
			case FRYER:			return "Fryer";
			case GRILL_PRESS:	return "Grill Press";
			case OVEN:			return "Oven";
			default:			return "INVALID MACHINE";
		}
	}
}