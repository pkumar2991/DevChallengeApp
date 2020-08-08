package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.EmailNotificationTemplate;
import com.db.awmd.challenge.domain.EmailNotificationTemplate.TransactionType;
import com.db.awmd.challenge.domain.TransferAmountRequest;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.TransferAmountException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

import java.math.BigDecimal;

import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private final AccountsService accountsService;

	@Autowired
	public AccountsController(AccountsService accountsService) {
		this.accountsService = accountsService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);

		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(path = "/{accountId}")
	public Account getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		return this.accountsService.getAccount(accountId);
	}

	@PostMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> transferAmount(@RequestBody TransferAmountRequest request) {
		String amount = request.getAmount();
		String accountFromId = request.getFromAccountId();
		String accountToId = request.getToAccountId();
		log.info("Transfer process for amount {} initiated from account {} to {}", amount, accountFromId, accountToId);
		boolean isSuccessful=false;
		try {
			isSuccessful = this.accountsService.transferAmount(accountFromId, accountToId, amount);
			if(isSuccessful) {
				return new ResponseEntity<>("Amount transferred successfully.", HttpStatus.OK);
			}else {
				throw new TransferAmountException("Transaction failed due to unknown reason.");
			}
		} catch (TransferAmountException te) {
			return new ResponseEntity<Object>(te.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

}
