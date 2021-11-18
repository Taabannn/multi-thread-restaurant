package multiThreadRestaurant.exceptions;

/**
 * @author Taban Soleymani
 */
public class IllegalRestaurantStateException extends RestaurantException {
    public IllegalRestaurantStateException(String message, int errorCode) {
        super(message, errorCode);
    }
}
