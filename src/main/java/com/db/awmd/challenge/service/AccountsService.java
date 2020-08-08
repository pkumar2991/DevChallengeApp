package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.EmailNotificationTemplate;
import com.db.awmd.challenge.domain.EmailNotificationTemplate.TransactionType;
import com.db.awmd.challenge.exception.TransferAmountException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	private final NotificationService notificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/**
	 * Thread Safe method to transfer balance from one account to another account
	 * @param accountFromId
	 * @param accountToId
	 * @param amount
	 * @return true if the transaction is successful otherwise returns false.
	 * @throws TransferAmountException
	 */
	public synchronized boolean transferAmount(String accountFromId, String accountToId, String amount)
			throws TransferAmountException {
		boolean areValidAccounts = validateAccounts(accountFromId, accountToId);
		if (areValidAccounts) {
			BigDecimal amountToBeTransferred = BigDecimal.ZERO;
			Account fromAccount = this.getAccountsRepository().getAccount(accountFromId);
			Account toAccount = this.getAccountsRepository().getAccount(accountToId);
			if (amount != null) {
				try {
					amountToBeTransferred = new BigDecimal(amount);
				} catch (NumberFormatException ne) {
					throw new TransferAmountException("Invalid amount.Please enter digits only for amount", ne);
				}
			}
			BigDecimal fromAccountCurrentBalance = fromAccount.getBalance();
			if (fromAccountCurrentBalance.compareTo(amountToBeTransferred) >= 0) {
				fromAccount.setBalance(fromAccountCurrentBalance.subtract(amountToBeTransferred));
				toAccount.setBalance(toAccount.getBalance().add(amountToBeTransferred));
				// Send notifications to account holders
				sendEmailNotification(fromAccount, toAccount, amount);
				return true;
			} else {
				throw new TransferAmountException("Insufficient balance in the account. We don't support overdraft.");
			}
		} else {
			throw new TransferAmountException("Account does not exist.Please provide valid accounts.");
		}

	}

	/**
	 * Validates if the provided accounts exist in the Bank DB.
	 * @param accounts
	 * @return true if all accounts are valid otherwise returns false
	 */
	private boolean validateAccounts(String... accounts) {
		long inValidAccount = Stream.of(accounts).filter(account -> {
			return this.accountsRepository.getAccount(account) == null;
		}).count();
		if (inValidAccount == 0) // All the given accounts are valid
			return true;
		return false;
	}

	/**
	 * Triggers email notification for each account holder which were involved in the transaction.
	 * @param fromAccount
	 * @param toAccount
	 * @param amount
	 */
	private synchronized void sendEmailNotification(Account fromAccount, Account toAccount, String amount) {
		EmailNotificationTemplate emailNotificationForSender = new EmailNotificationTemplate(amount,
				toAccount.getAccountId(), fromAccount.getAccountId(), TransactionType.DEBIT);
		notificationService.notifyAboutTransfer(fromAccount, emailNotificationForSender.getEmailMessage());
		EmailNotificationTemplate emailNotificationForReceiver = new EmailNotificationTemplate(amount,
				toAccount.getAccountId(), fromAccount.getAccountId(), TransactionType.CREDIT);
		notificationService.notifyAboutTransfer(fromAccount, emailNotificationForReceiver.getEmailMessage());
	}
}
