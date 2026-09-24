package com.burstlinker.ezcapsolver.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * What {@code /getBalance} returns.
 *
 * <p>Public for the same reason as the other envelopes in this package, though callers rarely
 * need it: {@code getBalance()} hands back the number itself.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class QueryBalanceResponse extends ResponseMeta {

    /** The account balance. {@code null} when the response carried no balance at all. */
    private BigDecimal balance;
}
