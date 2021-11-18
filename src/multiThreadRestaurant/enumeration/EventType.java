package multiThreadRestaurant.enumeration;

/**
 * @author Taban Soleymani
 */
public enum EventType {
    /* General events */
    SimulationStarting,
    SimulationEnded,
    /* Customer events */
    CustomerStarting,
    CustomerEnteredRestaurant,
    CustomerPlacedOrder,
    CustomerReceivedOrder,
    CustomerLeavingRestaurant,
    /* Cook Events */
    CookStarting,
    CookReceivedOrder,
    CookStartedFood,
    CookFinishedFood,
    CookCompletedOrder,
    CookEnding,
    /* Machine events */
    MachineStarting,
    MachineStartingFood,
    MachineDoneFood,
    MachineEnding
};
