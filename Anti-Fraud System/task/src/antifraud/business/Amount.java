package antifraud.business;

public class Amount {
    private long amount;

    public static long ALLOWED_LIMIT = 200l;
    public static long PROHIBITED_LIMIT = 1500l;

    public static String ALLOWED = "ALLOWED";
    public static String MANUAL_PROCESSING = "MANUAL_PROCESSING";
    public static String PROHIBITED = "PROHIBITED";

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public boolean validate(){
        return this.amount > 0l;
    }

    public String processingType() {

        if (this.amount <= ALLOWED_LIMIT) {
            return ALLOWED;
        } else if (this.amount <= PROHIBITED_LIMIT) {
            return MANUAL_PROCESSING;

        }
        return PROHIBITED;
    }
}
