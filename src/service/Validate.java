package service;

import multiThreadRestaurant.models.Cook;
import multiThreadRestaurant.models.Customer;
import multiThreadRestaurant.models.Food;
import multiThreadRestaurant.enumeration.EventType;
import multiThreadRestaurant.enumeration.FoodType;
import multiThreadRestaurant.enumeration.MachineType;
import multiThreadRestaurant.exceptions.InvalidSimulationException;
import multiThreadRestaurant.models.Machine;
import service.SimulationEvent;

import java.util.*;

public class Validate {
    private static void check(boolean check, String message) {
        if (!check) {
            throw new InvalidSimulationException("SIMULATION INVALID : " + message, 400);
        }
    }

    private static final Food[] foodArr = { FoodType.burger, FoodType.fries, FoodType.coffee, FoodType.chicken };
    private static HashMap<String, EventType> customerStates;
    private static HashMap<String, EventType> cookStates;
    private static HashMap<String, EventType> machineStates;

    private static String stateErrorMsg(String name, EventType oldEvent, EventType newEvent) {
        return name + " tried to switch from " + oldEvent + " to " + newEvent;
    }

    private static void updateCustomerState(String name, EventType newEvent) {
        EventType oldEvent = customerStates.get(name);
        switch(newEvent) {
            case CustomerStarting:
                check(oldEvent == null, stateErrorMsg(name, oldEvent, newEvent));
                break;
            case CustomerEnteredRestaurant:
                check(oldEvent == EventType.CustomerStarting, stateErrorMsg(name, oldEvent, newEvent));
                break;
            case CustomerPlacedOrder:
                check(oldEvent == EventType.CustomerEnteredRestaurant, stateErrorMsg(name, oldEvent, newEvent));
                break;
            case CustomerReceivedOrder:
                check(oldEvent == EventType.CustomerPlacedOrder, stateErrorMsg(name, oldEvent, newEvent));
                break;
            case CustomerLeavingRestaurant:
                check(oldEvent == EventType.CustomerReceivedOrder, stateErrorMsg(name, oldEvent, newEvent));
                break;
            default:
                check(false, "Illegal customer state");
        }

        customerStates.put(name, newEvent);
    }

    private static void updateCookState(String name, EventType newEvent) {
        EventType oldEvent = cookStates.get(name);
        switch(newEvent) {
            case CookStarting:
                check(oldEvent == null, stateErrorMsg(name, oldEvent, newEvent));
                break;
            case CookReceivedOrder:
                check(oldEvent != EventType.CookEnding, stateErrorMsg(name, oldEvent, newEvent));
                break;
            case CookStartedFood:
                check( oldEvent != EventType.CookEnding, stateErrorMsg(name, oldEvent, newEvent));
                break;
            case CookFinishedFood:
                check(oldEvent != EventType.CookEnding && oldEvent != EventType.CookStarting, stateErrorMsg(name, oldEvent, newEvent));
                break;
            case CookCompletedOrder:
                check(oldEvent != EventType.CookEnding, stateErrorMsg(name, oldEvent, newEvent));
                break;
            case CookEnding:
                check(oldEvent == EventType.CookStarting || oldEvent == EventType.CookCompletedOrder, stateErrorMsg(name, oldEvent, newEvent));
                break;
            default:
                check(false, "Illegal cook state");
        }

        cookStates.put(name, newEvent);
    }

    private static void updateMachineState(MachineType machType, EventType newEvent) throws InvalidSimulationException {
        String name = machType.toString();
        EventType oldEvent = machineStates.get(name);
        switch(newEvent) {
            case MachineStarting:
                check(oldEvent == null, stateErrorMsg(name, oldEvent, newEvent));
                break;

            case MachineStartingFood:
                check(oldEvent != EventType.MachineEnding, stateErrorMsg(name, oldEvent, newEvent));
                break;

            case MachineDoneFood:
                check(oldEvent != EventType.MachineEnding && oldEvent != EventType.MachineStarting, stateErrorMsg(name, oldEvent, newEvent));
                break;

            case MachineEnding:
                check(oldEvent == EventType.MachineStarting || oldEvent == EventType.MachineDoneFood, stateErrorMsg(name, oldEvent, newEvent));
                break;

            default:
                check(false, "Illegal machine state");
        }

        machineStates.put(name, newEvent);
    }

    private static void sameOrderCheck(List<Food> order1, List<Food> order2, String errorMsg) {
        HashMap<Food, Integer> foodCounts = new HashMap<>();
        for (Food item : foodArr)
            foodCounts.put(item,  0);

        for (Food item : order1)
            foodCounts.put(item,  foodCounts.get(item) + 1);
        for (Food item : order2)
            foodCounts.put(item,  foodCounts.get(item) - 1);

        for (Integer count : foodCounts.values())
            check(count == 0, errorMsg + "\n" + order1 + "\n" + order2);

    }

    public static boolean validateSimulation(List<SimulationEvent> events) {
        try {
            check(events.get(0).event == EventType.SimulationStarting,
                    "Simulation didn't start with initiation event \n " +
                            events.get(0).event);
            check(events.get(events.size()-1).event ==
                            EventType.SimulationEnded,
                    "Simulation didn't end with termination event \n" +
                            events.get(events.size()-1).event);


            int numCustomers = events.get(0).simParams[0];
            int numTables = events.get(0).simParams[2];
            int numCooks = events.get(0).simParams[1];
            int customers = 0;
            int customersIn = 0;
            int cooksReported = 0;
            for (int i = 0; i < events.size(); i++) {
                SimulationEvent e = events.get(i);
                if (e.event == EventType.CustomerEnteredRestaurant) {
                    customersIn++;
                } else if (e.event == EventType.CustomerLeavingRestaurant) {
                    customersIn--;
                } else if (e.event == EventType.CustomerStarting) {
                    customers++;
                } else if (e.event == EventType.CookStarting) {
                    cooksReported++;
                }

                check(customersIn <= numTables, "More customers in Restaurant than tables.");
            }

            check(numCustomers == customers, "More eaters have shown up.");

            check(numCooks == cooksReported, "More or less cooks reported for work.");


            int machineCapacity = events.get(0).simParams[3];
            int fryerCurrCapacity = 0;
            int ovenCurrCapacity = 0;
            int grillCurrCapacity = 0;
            int fountainCurrCapacity = 0;

            for (int i = 0; i < events.size(); i++) {
                SimulationEvent e = events.get(i);
                if (e.event == EventType.MachineStartingFood) {
                    Machine machine = e.machine;
                    if (machine.machineFoodType == FoodType.burger) {
                        fryerCurrCapacity++;
                        check(fryerCurrCapacity <= machineCapacity, "Fryer capacity is greater than machine");
                    } else if (machine.machineFoodType == FoodType.fries) {
                        ovenCurrCapacity++;
                        check(ovenCurrCapacity <= machineCapacity, "Oven capacity is greater than machine");
                    } else if (machine.machineFoodType == FoodType.coffee) {
                        grillCurrCapacity++;
                        check(grillCurrCapacity <= machineCapacity, "Grill capacity is greater than machine");
                    } else if (machine.machineFoodType == FoodType.chicken) {
                        fountainCurrCapacity++;
                        check(fountainCurrCapacity <= machineCapacity, "Fountain capacity is greater than machine");
                    }
                } else if (e.event == EventType.MachineDoneFood) {
                    Machine machine = e.machine;
                    if (machine.machineFoodType == FoodType.burger) {
                        fryerCurrCapacity--;
                        check(fryerCurrCapacity <= machineCapacity, "Fryer capacity is greater than machine");
                    } else if (machine.machineFoodType == FoodType.fries) {
                        ovenCurrCapacity--;
                        check(ovenCurrCapacity <= machineCapacity, "Oven capacity is greater than machine");
                    } else if (machine.machineFoodType == FoodType.coffee) {
                        grillCurrCapacity--;
                        check(grillCurrCapacity <= machineCapacity, "Grill capacity is greater than machine");
                    } else if (machine.machineFoodType == FoodType.chicken) {
                        fountainCurrCapacity--;
                        check(fountainCurrCapacity <= machineCapacity, "Fountain capacity is greater than machine");
                    }
                }
            }


            ArrayList<Integer> currOrders = new ArrayList<>(events.get(0).simParams[0]);
            for (int i = 0; i < events.size(); i++) {
                SimulationEvent e = events.get(i);
                if (e.event == EventType.CookCompletedOrder) {
                    currOrders.add(e.orderNumber);
                }
                if (e.event == EventType.CustomerReceivedOrder) {
                    check(currOrders.contains(e.orderNumber), "Customer received an order before cook completes it. \n" +
                            e.customer + " " + e.orderNumber);
                }
            }


            ArrayList<Customer> currCustomers = new ArrayList<>(events.get(0).simParams[0]);
            for (int i = 0; i < events.size(); i++) {
                SimulationEvent e = events.get(i);
                if (e.event == EventType.CustomerReceivedOrder) {
                    currCustomers.add(e.customer);
                }
                if (e.event == EventType.CustomerLeavingRestaurant) {
                    check(currCustomers.contains(e.customer), "Customer left before receiving an order. \n" +
                            e.customer);
                }
            }


            ArrayList<Customer> currCustomersWhoHaveOrdered = new ArrayList<>(events.get(0).simParams[0]);
            for (int i = 0; i < events.size(); i++) {
                SimulationEvent e = events.get(i);
                if (e.event == EventType.CustomerPlacedOrder) {
                    check(!currCustomersWhoHaveOrdered.contains(e.customer), "Customer is placing more than one orders. \n" +
                            e.customer);
                    currCustomersWhoHaveOrdered.add(e.customer);
                }
            }


            ArrayList<Integer> ordersPlaced = new ArrayList<>(events.get(0).simParams[0]);
            for (int i = 0; i < events.size(); i++) {
                SimulationEvent e = events.get(i);
                if (e.event == EventType.CustomerPlacedOrder) {
                    ordersPlaced.add(e.orderNumber);
                }
                if (e.event == EventType.CookReceivedOrder) {
                    check(ordersPlaced.contains(e.orderNumber), "Cook started order before it was received. \n" +
                            e.cook + " " + e.orderNumber + "    " + e.toString());
                }
            }


            HashMap<Cook, List<Food>> cookList = new HashMap<>();
            HashMap<Cook, Integer> orderList = new HashMap<>();
            HashMap<Cook, List<Food>> cookingList = new HashMap<>();
            for (int i = 0; i < events.size(); i++) {
                SimulationEvent e = events.get(i);
                if (e.event == EventType.CookReceivedOrder) {
                    cookList.put(e.cook, e.orderFood);
                    orderList.put(e.cook, e.orderNumber);
                } else if (e.event == EventType.CookFinishedFood) {
                    if (cookingList.containsKey(e.cook)) {
                        cookingList.get(e.cook).add(e.food);
                    } else {
                        ArrayList<Food> foodList = new ArrayList<>();
                        foodList.add(e.food);
                        cookingList.put(e.cook, foodList);
                    }
                } else if (e.event == EventType.CookCompletedOrder) {
                    check(e.orderNumber == orderList.get(e.cook), "Current cook orders do not match");
                    if (cookingList.get(e.cook) == null) {
                        cookingList.put(e.cook, new ArrayList<>());
                    }
                    check(equalLists(cookingList.get(e.cook), (cookList.get(e.cook))), "Finished cooking list does not match original \n" +
                            "cookingList = " + cookingList.get(e.cook) + " cookList = " + cookList.get(e.cook) + " ordernum = " + e.orderNumber);
                    cookList.remove(e.cook);
                    orderList.remove(e.cook);
                    cookingList.remove(e.cook);
                }
            }

            HashMap<Customer, List<Food>> customerOriginalOrders = new HashMap<>();
            for (int i = 0; i < events.size(); i++) {
                SimulationEvent e = events.get(i);
                if (e.event == EventType.CustomerPlacedOrder) {
                    customerOriginalOrders.put(e.customer, e.orderFood);
                }
                if (e.event == EventType.CustomerReceivedOrder) {
                    check(equalLists(e.orderFood, customerOriginalOrders.get(e.customer)), "Orginal and received orders do not match. \n" +
                            "Received: " + e.orderFood + " ordered: " + customerOriginalOrders.get(e.customer) + " " + e.orderNumber);
                }
            }

            ArrayList<Machine> doneMachines = new ArrayList<>();
            for (int i = 0; i < events.size(); i++) {
                SimulationEvent e = events.get(i);
                if (e.event == EventType.MachineEnding) {
                    doneMachines.add(e.machine);
                }
                if (e.event == EventType.MachineDoneFood) {
                    check(!doneMachines.contains(e.machine), "Machine ended before cooking was done \n" +
                            e);
                }
            }

            int[] simParams = events.get(0).simParams;
            int capacity = simParams[3];

            int numCustomersHandled = 0;
            int tableLoad = 0;

            HashMap<Food, Integer> foodLoad = new HashMap<>();

            HashMap<Integer, Integer> completedOrders = new HashMap<>();

            HashMap<Integer, Cook> orderHandlers = new HashMap<>();

            HashMap<Integer, List<Food>> completedItems = new HashMap<>();

            HashMap<Integer, List<Food>> allOrders = new HashMap<>();

            customerStates = new HashMap<>();
            cookStates = new HashMap<>();
            machineStates = new HashMap<>();

            for (Food item : foodArr)
                foodLoad.put(item,  0);

            for (int i = 0; i < events.size(); i++) {
                SimulationEvent e = events.get(i);
                switch(e.event) {
                    case SimulationStarting:
                        break;

                    case SimulationEnded:
                        break;

                    case CustomerStarting:
                        updateCustomerState(e.customer.toString(), e.event);
                        break;

                    case CustomerEnteredRestaurant:
                        check(++tableLoad <= numTables, "More customers in Restaurant than tables permit");
                        updateCustomerState(e.customer.toString(), e.event);
                        break;

                    case CustomerPlacedOrder:
                        check(allOrders.get(e.orderNumber) == null, "Order number has already been placed");
                        allOrders.put(e.orderNumber, e.orderFood);
                        updateCustomerState(e.customer.toString(), e.event);
                        break;

                    case CustomerReceivedOrder:
                        check(completedOrders.get(e.orderNumber) != null, "Customer received order before it was complete");
                        updateCustomerState(e.customer.toString(), e.event);
                        break;

                    case CustomerLeavingRestaurant:
                        check(--tableLoad >= 0, "Number of customers in Restaurant is negative");
                        updateCustomerState(e.customer.toString(), e.event);
                        numCustomersHandled++;
                        break;

                    case CookStarting:
                        updateCookState(e.cook.toString(), e.event);
                        break;

                    case CookReceivedOrder:
                        check(allOrders.get(e.orderNumber) != null, e.cook + " received order that was never placed");
                        check(orderHandlers.get(e.orderNumber) == null, e.cook + " received order that was already given to " + orderHandlers.get(e.orderNumber));
                        completedItems.put(e.orderNumber, new LinkedList<Food>());
                        orderHandlers.put(e.orderNumber, e.cook);
                        sameOrderCheck(e.orderFood,
                                allOrders.get(e.orderNumber),
                                "Mismatch between cook's received order and customer's placed order");
                        updateCookState(e.cook.toString(), e.event);
                        break;

                    case CookStartedFood:
                        check(foodLoad.get(e.food) < capacity, "Machine holding too much of " + e.food + "\n" + e + "\n" + foodLoad.get(e.food));
                        foodLoad.put(e.food, foodLoad.get(e.food) + 1);
                        check(orderHandlers.get(e.orderNumber) == e.cook, e.cook + " starting food for order that was started by " + orderHandlers.get(e.orderNumber));
                        check(completedOrders.get(e.orderNumber) == null, e.cook + " started food for an order that was already complete");
                        updateCookState(e.cook.toString(), e.event);
                        break;

                    case CookFinishedFood:
                        check(foodLoad.get(e.food) > 0, "Machine holding negative of " + e.food);
                        foodLoad.put(e.food, foodLoad.get(e.food) - 1);
                        check(orderHandlers.get(e.orderNumber) == e.cook, e.cook + " finished food for order that was started by " + orderHandlers.get(e.orderNumber));
                        check(completedOrders.get(e.orderNumber) == null, e.cook + " finished food for an order that was already complete");
                        completedItems.get(e.orderNumber).add(e.food);
                        updateCookState(e.cook.toString(), e.event);
                        break;

                    case CookCompletedOrder:
                        check(orderHandlers.get(e.orderNumber) == e.cook, e.cook + " completed order that was started by " + orderHandlers.get(e.orderNumber));
                        check(completedOrders.get(e.orderNumber) == null, e.cook + " completed order that was already complete");
                        sameOrderCheck(completedItems.get(e.orderNumber),
                                allOrders.get(e.orderNumber),
                                "Mismatch between cook's completed order and customer's placed order");
                        completedOrders.put(e.orderNumber, e.orderNumber);
                        updateCookState(e.cook.toString(), e.event);
                        break;

                    case CookEnding:
                        check(numCustomersHandled == numCustomers, "Cook left before all the customers were handled");
                        updateCookState(e.cook.toString(), e.event);
                        break;

                    case MachineStarting:
                        updateMachineState(e.machine.machineType, e.event);
                        break;

                    case MachineStartingFood:
                        updateMachineState(e.machine.machineType, e.event);
                        break;

                    case MachineDoneFood:
                        updateMachineState(e.machine.machineType, e.event);
                        break;

                    case MachineEnding:
                        updateMachineState(e.machine.machineType, e.event);
                        break;

                    default:
                        throw new InvalidSimulationException("VALIDATION CODE DOES NOT HANDLE EVENT " + e.event, 400);
                }
            }

            Collection<EventType> finalCustomerStates = customerStates.values();
            Collection<EventType> finalCookStates = cookStates.values();
            Collection<EventType> finalMachineStates = machineStates.values();

            check(finalCustomerStates.size() == numCustomers, "Simulation expected " + numCustomers + " customers, but log records " + finalCustomerStates.size());
            check(finalCookStates.size() == numCooks, "Simulation expected " + numCooks + " cooks, but log records " + finalCookStates.size());
            check(finalMachineStates.size() == 4, "Simulation expected 4 machines, but log records" + finalMachineStates.size());

            for (EventType e : finalCustomerStates)
                check(e == EventType.CustomerLeavingRestaurant, "At end of log, not all customers have left Restaurant");
            for (EventType e : finalCookStates)
                check(e == EventType.CookEnding, "At end of log, not all cooks have ended");
            for (EventType e : finalMachineStates)
                check(e == EventType.MachineEnding, "At end of log, not all machines have shut down");

            check(completedOrders.size() == numCustomers, "At end of log, number of completed orders does not match up with number of customers");

            System.out.println(events.get(0));
            return true;
        } catch (InvalidSimulationException e) {
            return false;
        }
    }

    public static boolean equalLists(List<Food> one, List<Food> two) {
        if (one == null && two == null){
            return true;
        }

        if((one == null && two != null)
                || one != null && two == null
                || one.size() != two.size()){
            return false;
        }

        ArrayList<String> one2 = new ArrayList<>();
        for (Food food : one) {
            one2.add(food.toString());
        }
        ArrayList<String> two2 = new ArrayList<>();
        for (Food food : two) {
            two2.add(food.toString());
        }

        Collections.sort(one2);
        Collections.sort(two2);
        return one2.equals(two2);
    }
}
