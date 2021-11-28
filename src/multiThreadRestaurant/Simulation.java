package multiThreadRestaurant;

import multiThreadRestaurant.models.Cook;
import multiThreadRestaurant.models.Customer;
import multiThreadRestaurant.models.Food;
import multiThreadRestaurant.models.FoodType;
import multiThreadRestaurant.enumeration.MachineType;
import multiThreadRestaurant.models.Machine;
import multiThreadRestaurant.service.SimulationEvent;
import multiThreadRestaurant.service.Validate;

import java.util.*;

public class Simulation {
    private static List<SimulationEvent> events;
    public static HashMap<String, Machine> machines;
    private static int tables;
    private static final Object frontDoor = new Object(); // an instanceLock object for synchronizing on entering and leaving customers
    private static volatile int numCustomers = 0;
    private static final Object orderLock = new Object();
    private static LinkedList<Integer> orderList = new LinkedList<>();
    private static HashMap<Integer, List<Food>> ordersPlaced = new HashMap<>();
    private static HashMap<Cook, List<Food>> cookOrderClaim = new HashMap<>();
    private static HashMap<Cook, Integer> cookOrderNumClaim = new HashMap<>();
    private static final Object customerLock = new Object();
    private static List<Integer> completedOrders = new ArrayList<>();
    private static final Object cookingLock = new Object();
    private static HashMap<Integer, List<Food>> ordersCooked = new HashMap<>();
    private static HashMap<Integer, List<Food>> checkedOrdersCooked = new HashMap<>();

    public synchronized static void logEvent(SimulationEvent event) {
        events.add(event);
        System.out.println(event);
    }

    public static boolean handleCustomerEntrance() {
        synchronized (frontDoor) {
            if (numCustomers < tables) {
                numCustomers++;
                return true;
            } else {
                return false;
            }
        }
    }

    public static void handleCustomerLeft() {
        synchronized (frontDoor) {
            numCustomers--;
        }
    }

    public static void handlePlacedOrder(int orderNum, List<Food> order) {
        synchronized (orderLock) {
            orderList.add(orderNum);
            ordersPlaced.put(orderNum, order);
        }
    }

    public static boolean isOrderAvailable(Cook cook) {
        synchronized (orderLock) {
            if (!ordersPlaced.isEmpty()) {
                int orderNum = orderList.pop();
                cookOrderClaim.put(cook, ordersPlaced.remove(orderNum));
                cookOrderNumClaim.put(cook, orderNum);
                return true;
            } else {
                return false;
            }
        }
    }

    public static int handleCookGetOrderNum(Cook cook) {
        synchronized (orderLock) {
            return cookOrderNumClaim.remove(cook);
        }
    }

    public static List<Food> handleCookGettingOrder(Cook cook) {
        synchronized (orderLock) {
            return cookOrderClaim.remove(cook);
        }
    }

    public static void handleCompletedOrder(Cook cook, int orderNum) {
        synchronized (customerLock) {
            completedOrders.add(orderNum);
            Simulation.logEvent(SimulationEvent.cookCompletedOrder(cook, orderNum));
        }
    }

    public static boolean checkOrderStatus(int orderNum) {
        synchronized (customerLock) {
            return completedOrders.contains(orderNum);
        }
    }

    public static void updateCookedOrder(Machine machine, int orderNum, Food foodCooked) {
        synchronized (cookingLock) {
            if (ordersCooked.containsKey(orderNum)) {
                ordersCooked.get(orderNum).add(foodCooked);
            } else {
                List<Food> cookedFoodList = new ArrayList<>();
                cookedFoodList.add(foodCooked);
                ordersCooked.put(orderNum, cookedFoodList);
            }
            logEvent(SimulationEvent.machineDoneFood(machine, foodCooked));
        }
    }

    public static boolean checkCookingStatus(Cook cook, int orderNum, Food food) {
        synchronized (cookingLock) {
            if (ordersCooked.containsKey(orderNum)) {
                if(ordersCooked.get(orderNum).contains(food)) {
                    ordersCooked.get(orderNum).remove(food);
                    if (checkedOrdersCooked.containsKey(orderNum)) {
                        checkedOrdersCooked.get(orderNum).add(food);
                    } else {
                        List<Food> cookedFoodList = new ArrayList<>();
                        cookedFoodList.add(food);
                        checkedOrdersCooked.put(orderNum, cookedFoodList);
                    }
                    logEvent(SimulationEvent.cookFinishedFood(cook, food, orderNum));
                    machines.get(food.name).itemsCooking--;
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }

        }
    }

    public static List<Food> getCompletedOrder(int orderNum) {
        synchronized (cookingLock) {
            return checkedOrdersCooked.remove(orderNum);
        }
    }

	public static List<SimulationEvent> runSimulation( int numCustomers, int numCooks, int numTables, int machineCapacity, boolean randomOrders) {
        tables = numTables;
		events = Collections.synchronizedList(new ArrayList<>());
		logEvent(SimulationEvent.startSimulation(numCustomers, numCooks, numTables, machineCapacity));
        assignFoodTypesToMachineType(machineCapacity);
        Thread[] cooks = new Thread[numCooks];

        for (int i = 0; i < numCooks; i++) {
            cooks[i] = new Thread(new Cook("Cook " + (i + 1)));
            cooks[i].start();
        }

		Thread[] customers = new Thread[numCustomers];
		LinkedList<Food> order;
		if (!randomOrders) {
			order = new LinkedList<>();
			order.add(FoodType.burger);
			order.add(FoodType.fries);
			order.add(FoodType.coffee);
			order.add(FoodType.chicken);
			for(int i = 0; i < customers.length; i++) {
				customers[i] = new Thread(new Customer("Customer " + (i), order));
			}
		}
		else {
			for(int i = 0; i < customers.length; i++) {
				Random rnd = new Random();
				int burgersCount = rnd.nextInt(4);
				int friesCount = rnd.nextInt(4);
				int coffeeCount = rnd.nextInt(4);
				int chickenCount = rnd.nextInt(4);
				order = new LinkedList<>();
				for (int b = 0; b < burgersCount; b++) {
					order.add(FoodType.burger);
				}
				for (int f = 0; f < friesCount; f++) {
					order.add(FoodType.fries);
				}
				for (int f = 0; f < coffeeCount; f++) {
					order.add(FoodType.coffee);
				}
				for (int c = 0; c < chickenCount; c++) {
					order.add(FoodType.chicken);
				}
				customers[i] = new Thread(new Customer("Customer " + (i), order));
				customers[i].start();
			}
		}


		try {
			for (int i = 0; i < customers.length; i++) {
			    customers[i].join();
            }

			for(int i = 0; i < cooks.length; i++) {
			    cooks[i].interrupt();
            }

            for(int i = 0; i < cooks.length; i++) {
                cooks[i].join();
            }
		}
		catch(InterruptedException e) {
			System.out.println("Simulation thread has been interrupted.");
		}

        Set<String> machinesList = machines.keySet();
        for (String machine : machinesList) {
            logEvent(SimulationEvent.machineEnding(machines.get(machine)));
        }

		logEvent(SimulationEvent.endSimulation());

		return events;
	}

    public static void assignFoodTypesToMachineType(int machineCapacity) {
        machines = new HashMap<>();
        machines.put(FoodType.chicken.name, new Machine(MachineType.OVEN, FoodType.chicken, machineCapacity));
        logEvent(SimulationEvent.machineStarting(machines.get(FoodType.chicken.name), FoodType.chicken, machineCapacity));
        machines.put(FoodType.coffee.name, new Machine(MachineType.FOUNTAIN, FoodType.coffee, machineCapacity));
        logEvent(SimulationEvent.machineStarting(machines.get(FoodType.coffee.name), FoodType.coffee, machineCapacity));
        machines.put(FoodType.fries.name, new Machine(MachineType.FRYER, FoodType.fries, machineCapacity));
        logEvent(SimulationEvent.machineStarting(machines.get(FoodType.fries.name), FoodType.fries, machineCapacity));
        machines.put(FoodType.burger.name, new Machine(MachineType.GRILL_PRESS, FoodType.burger, machineCapacity));
        logEvent(SimulationEvent.machineStarting(machines.get(FoodType.burger.name), FoodType.burger, machineCapacity));
    }

    public static void main(String args[]) {
        try {
            int numCustomers = 10;
            int numCooks = 3;
            int numTables = 5;
            int machineCapacity = 4;
            boolean randomOrders = true;
            System.out.println("Did it work? " + Validate.validateSimulation(runSimulation(numCustomers, numCooks, numTables, machineCapacity, randomOrders)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
