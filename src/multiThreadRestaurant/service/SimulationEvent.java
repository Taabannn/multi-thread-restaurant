package multiThreadRestaurant.service;

import multiThreadRestaurant.models.Cook;
import multiThreadRestaurant.models.Customer;
import multiThreadRestaurant.models.Food;
import multiThreadRestaurant.enumeration.EventType;
import multiThreadRestaurant.exceptions.IllegalRestaurantStateException;
import multiThreadRestaurant.models.Machine;

import java.util.List;

public class SimulationEvent {

    public final EventType event;
    public final Cook cook;
    public final Customer customer;
    public final Machine machine;
    public final Food food;
    public final List<Food> orderFood;
    public final int orderNumber;
    public final int[] simParams;

    public SimulationEvent(EventType event, Cook cook, Customer customer, Machine machine, Food food, List<Food> orderFood, int orderNumber, int[] simParams) {
        this.event = event;
        this.cook = cook;
        this.customer = customer;
        this.machine = machine;
        this.food = food;
        this.orderFood = orderFood;
        this.orderNumber = orderNumber;
        this.simParams = simParams;
    }

    /* General events */
    public static SimulationEvent startSimulation(int numCustomers, int numCooks, int numTables, int capacity) {
        int[] params = new int[4];
        params[0] = numCustomers;
        params[1] = numCooks;
        params[2] = numTables;
        params[3] = capacity;
        return new SimulationEvent(EventType.SimulationStarting, null, null, null, null, null, 0, params);
    }

    public static SimulationEvent endSimulation() {
        return new SimulationEvent(EventType.SimulationEnded, null, null, null, null, null, 0, null);
    }

    /* Customer events */
    public static SimulationEvent customerStarting(Customer customer) {
        return new SimulationEvent(EventType.CustomerStarting, null, customer, null, null, null, 0, null);
    }

    public static SimulationEvent customerEnteredRestaurant(Customer customer) {
        return new SimulationEvent(EventType.CustomerEnteredRestaurant, null, customer, null, null, null, 0, null);
    }

    public static SimulationEvent customerPlacedOrder(Customer customer, List<Food> order, int orderNumber) {
        return new SimulationEvent(EventType.CustomerPlacedOrder, null, customer, null, null, order, orderNumber, null);
    }

    public static SimulationEvent customerReceivedOrder(Customer customer, List<Food> order, int orderNumber) {
        return new SimulationEvent(EventType.CustomerReceivedOrder, null, customer, null, null, order, orderNumber, null);
    }

    public static SimulationEvent customerLeavingRestaurant(Customer customer) {
        return new SimulationEvent(EventType.CustomerLeavingRestaurant, null, customer, null, null, null, 0, null);
    }

    /* Cook events */
    public static SimulationEvent cookStarting(Cook cook) {
        return new SimulationEvent(EventType.CookStarting, cook, null, null, null, null, 0, null);
    }

    public static SimulationEvent cookReceivedOrder(Cook cook, List<Food> order, int orderNumber) {
        return new SimulationEvent(EventType.CookReceivedOrder, cook, null, null, null, order, orderNumber, null);
    }

    public static SimulationEvent cookStartedFood(Cook cook, Food food, int orderNumber) {
        return new SimulationEvent(EventType.CookStartedFood, cook, null, null, food, null, orderNumber, null);
    }

    public static SimulationEvent cookFinishedFood(Cook cook, Food food, int orderNumber) {
        return new SimulationEvent(EventType.CookFinishedFood, cook, null, null, food, null, orderNumber, null);
    }

    public static SimulationEvent cookCompletedOrder(Cook cook, int orderNumber) {
        return new SimulationEvent(EventType.CookCompletedOrder, cook, null, null, null, null, orderNumber, null);
    }

    public static SimulationEvent cookEnding(Cook cook) {
        return new SimulationEvent(EventType.CookEnding, cook, null, null, null, null, 0, null);
    }

    /* Machine events */
    public static SimulationEvent machineStarting(Machine machine, Food food, int capacity) {
        int[] params = new int[1];
        params[0] = capacity;
        return new SimulationEvent(EventType.MachineStarting, null, null, machine, food, null, 0, params);
    }

    public static SimulationEvent machineCookingFood(Machine machine, Food food) {
        return new SimulationEvent(EventType.MachineStartingFood, null, null, machine, food, null, 0, null);
    }

    public static SimulationEvent machineDoneFood(Machine machine, Food food) {
        return new SimulationEvent(EventType.MachineDoneFood, null, null, machine, food, null, 0, null);
    }

    public static SimulationEvent machineEnding(Machine machine) {
        return new SimulationEvent(EventType.MachineEnding, null, null, machine, null, null, 0, null);
    }

    public String toString() {
        switch (event) {
            /* General events */
            case SimulationStarting:
                int numCustomers = simParams[0];
                int numCooks = simParams[1];
                int numTables = simParams[2];
                int capacity = simParams[3];
                return "Starting simulation: " + numCustomers + " customers; " +
                        numCooks + " cooks; " + numTables + " tables; " +
                        "machine capacity " + capacity + ".";

            case SimulationEnded:
                return "Simulation ended.";

            /* Customer events */
            case CustomerStarting:
                return customer + " going to Restaurant.";

            case CustomerEnteredRestaurant:
                return customer + " entered Restaurant.";

            case CustomerPlacedOrder:
                return customer + " placing order " + orderNumber + " " + orderFood;

            case CustomerReceivedOrder:
                return customer + " received order " + orderNumber + " " + orderFood;

            case CustomerLeavingRestaurant:
                return customer + " leaving Restaurant.";

            /* Cook Events */
            case CookStarting:
                return cook + " reporting for work.";

            case CookReceivedOrder:
                return cook + " starting order " + orderNumber + " " + orderFood;

            case CookStartedFood:
                return cook + " preparing " + food + " for order " + orderNumber;

            case CookFinishedFood:
                return cook + " finished " + food + " for order " + orderNumber;

            case CookCompletedOrder:
                return cook + " completed order " + orderNumber;

            case CookEnding:
                return cook + " going home for the night.";

            /* Machine events */
            case MachineStarting:
                return machine + " starting up for making " +
                        food + "; " + simParams[0] + ".";

            case MachineStartingFood:
                return machine + " making " + food + ".";

            case MachineDoneFood:
                return machine + " completed " + food + ".";

            case MachineEnding:
                return machine + " shutting down.";

            default:
                throw new IllegalRestaurantStateException("Illegal event. Unable to recognize this event.", 400);
        }
    }
}
