package com.delfin.jarexp.win.exe;

import java.math.BigInteger;


abstract class UNumber extends Number {

	private static final long serialVersionUID = -8013295934738722551L;

	abstract BigInteger toBigInteger();

	abstract String toHexString();
}
