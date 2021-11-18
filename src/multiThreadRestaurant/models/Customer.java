package multiThreadRestaurant.models;

import service.Simulation;
import service.SimulationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Customer implements Runnable {
	private final String name;
	private final List<Food> order;
	private final int orderNum;
	private static int runningCounter = 0;

	public Customer(String name, List<Food> order) {
		this.name = name;
		this.order = order;
		this.orderNum = ++runningCounter;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return orderNum == customer.orderNum && name.equals(customer.name) && order.equals(customer.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, order, orderNum);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                ", order=" + order +
                ", orderNum=" + orderNum +
                '}';
    }

    public void run() {
        Simulation.logEvent(SimulationEvent.customerStarting(this));
        boolean entered = false;
        while (!entered) {
            entered = Simulation.handleCustomerEntrance();
        }
        Simulation.logEvent(SimulationEvent.customerEnteredRestaurant(this));

        Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, new ArrayList<>(this.order), this.orderNum));
        Simulation.handlePlacedOrder(this.orderNum, this.order);

        boolean customerWaiting = true;
        while (customerWaiting) {
            if (Simulation.checkOrderStatus(this.orderNum)) {
                customerWaiting = false;
                List<Food> orderComplete = Simulation.getCompletedOrder(this.orderNum);
                if (orderComplete == null) {
                    Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, new ArrayList<Food>(), orderNum));
                } else {
                    Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, orderComplete, orderNum));
                }
                Simulation.logEvent(SimulationEvent.customerLeavingRestaurant(this));
                Simulation.handleCustomerLeft();
            }
        }
	}
}