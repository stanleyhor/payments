package com.aeo.model.response.authorization;

import com.aeo.model.request.Address;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class Item {

    private String paymentHeaderId;
    private double amountAuthorized;
    private String submittedDate;
    private List<AuthorizationStatus> authorizationStatus;
    private String paymentMethod;
    private String gatewayIndicator;
    private String nameOnCard;
    private String creditCardType;
    private String creditCardNumber;
    private String creditCardNickName;
    private String cardAlias;
    private String cardToken;
    private String expirationMonth;
    private String expirationYear;
    private boolean fraudManualReview;
    private int fraudScore;
    private String fraudResultType;
    private Address billingAddress;
    private String currencyCode;
    private String giftCardNumber;
    private String maskedGiftCardNumber;
    private String initialBalance;
    private String giftCardType;
    private String sourceType;
    private String fundingSource;
    private boolean saveToProfile;
    private boolean cvvVerified;
    private String paymentAccountReference;
    private int installments;
    private String afterpayEventId;
    private String afterpayToken;
    private String afterpayId;
    private double afterpayInstallment;
    private String expires;

}
