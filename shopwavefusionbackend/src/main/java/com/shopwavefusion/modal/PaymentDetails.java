package com.shopwavefusion.modal;

import java.time.LocalDate;

import com.shopwavefusion.user.domain.PaymentMethod;
import com.shopwavefusion.user.domain.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Embeddable;

@Embeddable
public class PaymentDetails {

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method")
	private PaymentMethod paymentMethod;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status")
	private PaymentStatus status;

	@Column(name = "payment_id")
	private String paymentId;

	@Column(name = "cardholder_name")
	private String cardholderName;

	@Column(name = "card_number")
	private String cardNumber;

	@Column(name = "expiration_date")
	private LocalDate expirationDate;

	@Column(name = "cvv")
	private String cvv;

	public PaymentDetails() {
	}

	public PaymentDetails(PaymentMethod paymentMethod, PaymentStatus status, String paymentId, String cardholderName,
			String cardNumber, LocalDate expirationDate, String cvv) {
		this.paymentMethod = paymentMethod;
		this.status = status;
		this.paymentId = paymentId;
		this.cardholderName = cardholderName;
		this.cardNumber = cardNumber;
		this.expirationDate = expirationDate;
		this.cvv = cvv;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getCardholderName() {
		return cardholderName;
	}

	public void setCardholderName(String cardholderName) {
		this.cardholderName = cardholderName;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public LocalDate getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(LocalDate expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getCvv() {
		return cvv;
	}

	public void setCvv(String cvv) {
		this.cvv = cvv;
	}
}

