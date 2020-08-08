package com.db.awmd.challenge.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransferAmountRequest implements Serializable {
	/**
	 * 
	 */
	private String toAccountId;
	private String fromAccountId;
	private String amount;
}
