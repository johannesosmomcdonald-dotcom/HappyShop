package ci553.happyshop.catalogue.exceptions;

public class UnderMinimumPaymentException extends RuntimeException {
    public UnderMinimumPaymentException(String message) {
        super(message);
    }
    //exception class for less than £5 checkout
}
