package multiThreadRestaurant.exceptions;

/**
 * @author Taban Soleymani
 */
public class RestaurantException extends RuntimeException {
    int errorCode;

    public RestaurantException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
