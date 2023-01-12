package antifraud.business;


/**
 * Request body:
 *
 * {
 *    "transactionId": 1,
 *    "feedback": "ALLOWED"
 * }
 */
public class TransactionFeedback {

    private long transactionId;
    private String feedback;

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
