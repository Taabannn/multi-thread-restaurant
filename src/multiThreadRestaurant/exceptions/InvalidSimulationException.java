package multiThreadRestaurant.exceptions;

/**
 * @author Taban Soleymani
 */
public class InvalidSimulationException extends RestaurantException {
    public InvalidSimulationException(String message, int errorCode) {
        super(message, errorCode);
    }
}
