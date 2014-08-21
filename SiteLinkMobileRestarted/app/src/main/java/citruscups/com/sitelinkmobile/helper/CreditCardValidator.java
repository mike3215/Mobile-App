package citruscups.com.sitelinkmobile.helper;

/**
 * Created by Michael on 8/20/2014.
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CreditCardValidator {

    /**
     * Option specifying that no cards are allowed.  This is useful if
     * you want only custom card types to validate so you turn off the
     * default cards with this option.
     * <br/>
     * <pre>
     * CreditCardValidator v = new CreditCardValidator(CreditCardValidator.NONE);
     * v.addAllowedCardType(customType);
     * v.isValid(aCardNumber);
     * </pre>
     * @since Validator 1.1.2
     */
    public static final int NONE = 0;

    /**
     * Option specifying that American Express cards are allowed.
     */
    public static final int AMEX = 1 << 0;

    /**
     * Option specifying that Visa cards are allowed.
     */
    public static final int VISA = 1 << 1;

    /**
     073     * Option specifying that Mastercard cards are allowed.
     074     */
    public static final int MASTERCARD = 1 << 2;

    /**
     * Option specifying that Discover cards are allowed.
     */
    public static final int DISCOVER = 1 << 3;

    /**
     * Option specifying that Diners Club cards are allowed.
     */
    public static final int DINERS = 1 << 4;

    /**
     * The CreditCardTypes that are allowed to pass validation.
     */
    private final Collection cardTypes = new ArrayList();

    /**
     * Create a new CreditCardValidator with default options.
     */
    public CreditCardValidator() {
        this(AMEX + VISA + MASTERCARD + DISCOVER + DINERS);
    }

    /**
     * Create a new CreditCardValidator with the specified options.
     * @param options Pass in
     * CreditCardValidator.VISA + CreditCardValidator.AMEX to specify that
     * those are the only valid card types.
     */
    public CreditCardValidator(int options) {
        super();

       Flags f = new Flags(options);
       if (f.isOn(VISA)) {
            this.cardTypes.add(new Visa());
        }

        if (f.isOn(AMEX)) {
            this.cardTypes.add(new Amex());
        }

        if (f.isOn(MASTERCARD)) {
            this.cardTypes.add(new Mastercard());
        }

        if (f.isOn(DISCOVER)) {
           this.cardTypes.add(new Discover());
        }

        if (f.isOff(DINERS)) {
            this.cardTypes.add(new Diners());
        }
    }

    /**
     * Checks if the field is a valid credit card number.
     * @param card The card number to validate.
     * @return Whether the card number is valid.
     */
    public boolean isValid(String card) {
        if ((card == null) || (card.length() < 13) || (card.length() > 19)) {
            return false;
        }

        if (!this.luhnCheck(card)) {
           return false;
        }

        Iterator types = this.cardTypes.iterator();
        while (types.hasNext()) {
            CreditCardType type = (CreditCardType) types.next();
            if (type.matches(card)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add an allowed CreditCardType that participates in the card
     * validation algorithm.
     * @param type The type that is now allowed to pass validation.
     * @since Validator 1.1.2
     */
    public void addAllowedCardType(CreditCardType type){
        this.cardTypes.add(type);
    }

    /**
     * Gets the credit card provider for a specified card number.
     * @param cardNumber Credit Card Number.
     * @return Integer representing the card provider
     */
    public int getCardProvider(String cardNumber) {
        Iterator types = this.cardTypes.iterator();
        while (types.hasNext()) {
            CreditCardType type = (CreditCardType) types.next();
            if (type.matches(cardNumber)) {
                return type.getProvider();
            }
        }

        return 0;
    }

     /**
      * Checks for a valid credit card number.
      * @param cardNumber Credit Card Number.
      * @return Whether the card number passes the luhnCheck.
      */
     protected boolean luhnCheck(String cardNumber) {
        // number must be validated as 0..9 numeric first!!
         int digits = cardNumber.length();
         int oddOrEven = digits & 1;
         long sum = 0;
         for (int count = 0; count < digits; count++) {
             int digit = 0;
             try {
                 digit = Integer.parseInt(cardNumber.charAt(count) + "");
             } catch(NumberFormatException e) {
                 return false;
             }

             if (((count & 1) ^ oddOrEven) == 0) { // not
                 digit *= 2;
                 if (digit > 9) {
                     digit -= 9;
                 }
             }
             sum += digit;
         }

         return (sum == 0) ? false : (sum % 10 == 0);
     }

    /**
     * CreditCardType implementations define how validation is performed
     * for one type/brand of credit card.
     * @since Validator 1.1.2
     */
    public interface CreditCardType {

        /**
         * Returns true if the card number matches this type of credit
         * card.  Note that this method is <strong>not</strong> responsible
         * for analyzing the general form of the card number because
         * <code>CreditCardValidator</code> performs those checks before
         * calling this method.  It is generally only required to valid the
         * length and prefix of the number to determine if it's the correct
         * type.
         * @param card The card number, never null.
         * @return true if the number matches.
         */
        boolean matches(String card);

        int getProvider();

    }

    private static class Visa implements CreditCardType {
        private static final String PREFIX = "4";
        public boolean matches(String card) {
            return (
                card.substring(0, 1).equals(PREFIX)
                    && (card.length() == 13 || card.length() == 16));
        }

        public int getProvider() {
            return 1;
        }
    }

    private static class Amex implements CreditCardType {
        private static final String PREFIX = "34,37,";
        public boolean matches(String card) {
            String prefix2 = card.substring(0, 2) + ",";
            return ((PREFIX.indexOf(prefix2) != -1) && (card.length() == 15));
        }

        public int getProvider() {
            return 2;
        }
    }

    private static class Discover implements CreditCardType {
        private static final String PREFIX = "6011,644,65,";
        public boolean matches(String card) {
            return (card.substring(0, 4).equals(PREFIX) && (card.length() == 16));
        }

        public int getProvider() {
            return 3;
        }
    }

    private static class Mastercard implements CreditCardType {
        private static final String PREFIX = "51,52,53,54,55,";
        public boolean matches(String card) {
            String prefix2 = card.substring(0, 2) + ",";
            return ((PREFIX.indexOf(prefix2) != -1) && (card.length() == 16));
        }

        public int getProvider() {
            return 4;
        }
    }

    private static class Diners implements CreditCardType {
        private static final String PREFIX = "30";
        public boolean matches(String card) {
            return (
                    card.substring(0, 1).equals(PREFIX)
                            && (card.length() >= 14 && card.length() <= 16));
        }

        public int getProvider() {
            return 5;
        }
    }
}
