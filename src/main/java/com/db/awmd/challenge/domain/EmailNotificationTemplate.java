package com.db.awmd.challenge.domain;

public class EmailNotificationTemplate {
	public enum TransactionType {
		CREDIT, DEBIT
	}

	private final String amount;
	private final String toAccount;
	private final String fromAccount;
	private final TransactionType transactionType;

	public EmailNotificationTemplate(String amount, String toAccount, String fromAccount,
			TransactionType transactionType) {
		super();
		this.amount = amount;
		this.toAccount = toAccount;
		this.fromAccount = fromAccount;
		this.transactionType = transactionType;
	}
	
	public String getEmailMessage() {
		StringBuilder message = new StringBuilder();
		message.append("Dear Customer,");
		message.append("This email is regarding your recent transaction.");
		
		if(transactionType == TransactionType.CREDIT) {
			message.append("Rs. "+amount +" is credited to your account("+ toAccount+") from account " + fromAccount);
		}else {
			message.append("Rs. "+amount +" is debited from your account("+ fromAccount+")  and transferred to account " + toAccount);
		}
		return message.toString();
	}
}
